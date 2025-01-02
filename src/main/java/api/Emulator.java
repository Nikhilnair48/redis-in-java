package api;

public interface Emulator {
    void set(String key, String value, Integer ttl);
    String get(String key);
    void hset(String hashName, String field, String value, Integer ttl);
    String hget(String key, String field);
    void del(String key);
    void saveToFile(String filename);
    void loadFromFile(String filename);
}