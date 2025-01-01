package service;

import api.Emulator;
import model.HashWrapper;
import model.StringWrapper;

import java.util.HashMap;

public class RedisEmulator implements Emulator {
    private HashMap<String, StringWrapper> stringStorage;
    private HashMap<String, HashWrapper> hashStorage;

    public RedisEmulator() {
        this.stringStorage = new HashMap<>();
        this.hashStorage = new HashMap<>();
    }

    public RedisEmulator(HashMap<String, StringWrapper> stringStorage, HashMap<String, HashWrapper> hashStorage) {
        this.stringStorage = stringStorage;
        this.hashStorage = hashStorage;
    }

    @Override
    public void set(String key, String value, Integer ttl) {
        if (ttl == null) {
            stringStorage.put(key, new StringWrapper(value, null));
        } else {
            long expirationTime = System.currentTimeMillis() + (ttl * 1000L);
            stringStorage.put(key, new StringWrapper(value, expirationTime));
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
                // Expired key
                stringStorage.remove(key);
                return null;
            }
        }

        return wrapper.getValue();
    }

    @Override
    public void hset(String hashName, String field, String value, Integer ttl) {
        HashWrapper existingHash = hashStorage.get(hashName);

        if (existingHash == null) {
            existingHash = new HashWrapper(new HashMap<>());
            hashStorage.put(hashName, existingHash);
        }

        long expirationTime = (ttl == null) ? -1 : (System.currentTimeMillis() + ttl * 1000L);
        StringWrapper wrapper = new StringWrapper(value, (ttl == null) ? null : expirationTime);

        existingHash.getFields().put(field, wrapper);
    }

    @Override
    public String hget(String hashName, String field) {
        HashWrapper existingHash = hashStorage.get(hashName);
        if (existingHash == null) {
            return null;
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
}
