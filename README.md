# Redis Clone

A basic implementation of Redis in Java

---

## Features

- **String Keys**
    - `set(key, value, ttl)`: Stores a string key-value pair, optionally with a time-to-live
    - `get(key)`: Retrieves the value for a given key; returns `null` if the key does not exist or is expired

- **Hashes**
    - `hset(hashName, field, value, ttl)`: Stores a field-value pair in a hash, optionally with TTL at the field level
    - `hget(hashName, field)`: Retrieves a field’s value; returns `null` if it doesn’t exist or is expired

- **Memory Limits (LRU Eviction)**
    - A `maxMemory` parameter to restrict the total number of keys (string or hash)
    - Once capacity is reached, the LRU key is evicted

- **Persistence**
    - `saveToFile(filename)`: Serializes in-memory state to disk in JSON
    - `loadFromFile(filename)`: Restores the state from disk

- **Concurrency**
    - Supports multiple threads safely calling `set`, `get`, `hset`, `hget` in parallel
    - Uses `ConcurrentHashMap` for the string & hash storages and a lock around the LRU data structure

---

## How to Run

1. **Clone the Repository**
```bash
   git clone https://github.com/Nikhilnair48/redis-in-java.git
   cd redis-in-java
   mvn clean package
```