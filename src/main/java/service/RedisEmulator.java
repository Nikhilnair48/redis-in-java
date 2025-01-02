package service;

import api.Emulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.EmulatorState;
import model.HashWrapper;
import model.StringWrapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisEmulator implements Emulator {
    // stores for string and hash keys
    private ConcurrentHashMap<String, StringWrapper> stringStorage;
    private ConcurrentHashMap<String, HashWrapper> hashStorage;
    // map for LRU implementation, tracking order of usage of a key
    private LinkedHashMap<String, Boolean> lruOrder;
    private final Object lruLock = new Object();
    private int maxMemory;
    private int currentSize;

    public RedisEmulator(int maxMemory) {
        this.stringStorage = new ConcurrentHashMap<>();
        this.hashStorage = new ConcurrentHashMap<>();
        this.maxMemory = maxMemory;
        this.currentSize = 0;
        this.lruOrder = new LinkedHashMap<>(16, 0.75f, true);
    }

    public RedisEmulator() {
        this(Integer.MAX_VALUE);
    }

    private boolean isNewKey(String key) {
        return (!stringStorage.containsKey(key) && !hashStorage.containsKey(key));
    }

    private void performLRUEviction() {
        synchronized (lruLock) {
            if (currentSize == maxMemory) {
                String oldestKey = lruOrder.entrySet().iterator().next().getKey();
                lruOrder.remove(oldestKey);

                if (stringStorage.containsKey(oldestKey)) {
                    stringStorage.remove(oldestKey);
                } else if (hashStorage.containsKey(oldestKey)) {
                    hashStorage.remove(oldestKey);
                }
                currentSize--;
            }
        }
    }

    @Override
    public void set(String key, String value, Integer ttl) {
        boolean isNewKey = isNewKey(key);
        if (isNewKey) {
            performLRUEviction();
        }

        if (ttl == null) {
            stringStorage.put(key, new StringWrapper(value, null));
        } else {
            long expirationTime = System.currentTimeMillis() + (ttl * 1000L);
            stringStorage.put(key, new StringWrapper(value, expirationTime));
        }

        synchronized (lruLock) {
            lruOrder.put(key, Boolean.TRUE);
            if (isNewKey) {
                currentSize++;
            }
        }
    }

    @Override
    public String get(String key) {
        StringWrapper wrapper = stringStorage.get(key);
        if (wrapper == null) {
            return null;
        }

        if (wrapper.getTtl() != null) {
            long currentTime = System.currentTimeMillis();
            long expirationTime = wrapper.getTtl();
            if (currentTime >= expirationTime) {
                // Expired key - clear storage & cache
                stringStorage.remove(key);
                synchronized (lruLock) {
                    lruOrder.remove(key);
                    currentSize--;
                }
                return null;
            }
        }

        synchronized (lruLock) {
            lruOrder.get(key);
        }

        return wrapper.getValue();
    }

    @Override
    public void hset(String hashName, String field, String value, Integer ttl) {
        boolean isNewKey = isNewKey(hashName);
        if (isNewKey) {
            performLRUEviction();
        }

        HashWrapper existingHash = hashStorage.get(hashName);
        if (existingHash == null) {
            HashWrapper newHash = new HashWrapper(new ConcurrentHashMap<>());
            existingHash = hashStorage.putIfAbsent(hashName, newHash);
            if (existingHash == null) {
                existingHash = newHash;
            }
        }

        long expirationTime = (ttl == null) ? -1 : (System.currentTimeMillis() + ttl * 1000L);
        StringWrapper wrapper = new StringWrapper(value, (ttl == null) ? null : expirationTime);

        existingHash.getFields().put(field, wrapper);

        synchronized (lruLock) {
            lruOrder.put(hashName, Boolean.TRUE);
            if (isNewKey) {
                currentSize++;
            }
        }
    }

    @Override
    public String hget(String hashName, String field) {
        HashWrapper existingHash = hashStorage.get(hashName);
        if (existingHash == null) {
            return null;
        }

        synchronized (lruLock) {
            lruOrder.get(hashName);
        }

        StringWrapper wrapper = existingHash.getFields().get(field);
        if (wrapper == null) {
            return null;
        }

        if (wrapper.getTtl() != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= wrapper.getTtl()) {
                // Expired field
                existingHash.getFields().remove(field);
                return null;
            }
        }

        return wrapper.getValue();
    }

    @Override
    public void saveToFile(String filename) {
        synchronized (lruLock) {
            EmulatorState state = new EmulatorState(maxMemory);
            state.setStringStorage(stringStorage);
            state.setHashStorage(hashStorage);
            state.setCurrentSize(currentSize);

            state.setLruKeys(List.copyOf(lruOrder.keySet()));

            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(new File(filename), state);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadFromFile(String filename) {
        synchronized (lruLock) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                EmulatorState loadedState = mapper.readValue(new File(filename), EmulatorState.class);

                Map<String, StringWrapper> fromFileStringStorage = loadedState.getStringStorage();
                this.stringStorage = new ConcurrentHashMap<>();
                this.stringStorage.putAll(fromFileStringStorage);

                Map<String, HashWrapper> fromFileHashStorage = loadedState.getHashStorage();
                this.hashStorage = new ConcurrentHashMap<>();
                this.hashStorage.putAll(fromFileHashStorage);

                this.maxMemory = loadedState.getMaxMemory();
                this.currentSize = loadedState.getCurrentSize();

                this.lruOrder = new LinkedHashMap<>(16, 0.75f, true);
                List<String> keys = loadedState.getLruKeys();
                for (String key : keys) {
                    lruOrder.put(key, Boolean.TRUE);
                }

                // Consider: after loading, should we remove expired keys?
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
