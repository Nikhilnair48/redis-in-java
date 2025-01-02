package service;

import api.Emulator;
import model.HashWrapper;
import model.StringWrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class RedisEmulator implements Emulator {
    // stores for string and hash keys
    private HashMap<String, StringWrapper> stringStorage;
    private HashMap<String, HashWrapper> hashStorage;
    // map for LRU implementation, tracking order of usage of a key
    private LinkedHashMap<String, Boolean> lruOrder;
    private int maxMemory;
    private int currentSize;

    public RedisEmulator(int maxMemory) {
        this.stringStorage = new HashMap<>();
        this.hashStorage = new HashMap<>();
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

        lruOrder.put(key, Boolean.TRUE);
        if (isNewKey) {
            currentSize++;
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
                lruOrder.remove(key);
                currentSize--;
                return null;
            }
        }

        lruOrder.get(key);

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
            existingHash = new HashWrapper(new HashMap<>());
            hashStorage.put(hashName, existingHash);
        }

        long expirationTime = (ttl == null) ? -1 : (System.currentTimeMillis() + ttl * 1000L);
        StringWrapper wrapper = new StringWrapper(value, (ttl == null) ? null : expirationTime);

        existingHash.getFields().put(field, wrapper);

        lruOrder.put(hashName, Boolean.TRUE);
        if(isNewKey) {
            currentSize++;
        }
    }

    @Override
    public String hget(String hashName, String field) {
        HashWrapper existingHash = hashStorage.get(hashName);
        if (existingHash == null) {
            return null;
        }

        lruOrder.get(hashName);

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
}
