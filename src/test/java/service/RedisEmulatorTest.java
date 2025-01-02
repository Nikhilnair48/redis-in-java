package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RedisEmulatorTest {

    /**
     * Test 1: Basic set and get
     */
    @Test
    public void testBasicSetAndGet() {
        RedisEmulator redis = new RedisEmulator();

        redis.set("name", "Alice", null);

        Assertions.assertEquals("Alice", redis.get("name"), "Test 1 Failed: Basic set/get not working");

        Assertions.assertNull(redis.get("age"), "Test 1 Failed: Non-existent key should return null");
    }

    /**
     * Test 2: Key expiration
     */
    @Test
    public void testKeyExpiration() throws InterruptedException {
        RedisEmulator redis = new RedisEmulator();

        redis.set("session", "12345", 2);

        Thread.sleep(3000);

        Assertions.assertNull(redis.get("session"), "Test 2 Failed: TTL expiration not working");
    }

    /**
     * Test 3: Hash set and get
     */
    @Test
    public void testHashSetAndGet() {
        RedisEmulator redis = new RedisEmulator();

        redis.hset("user:1000", "name", "Bob", null);
        redis.hset("user:1000", "age", "30", null);

        Assertions.assertEquals("Bob", redis.hget("user:1000", "name"), "Test 3 Failed: Hash set/get not working");

        Assertions.assertNull(redis.hget("user:1000", "email"), "Test 3 Failed: Non-existent field should return null");
    }

    /**
     * Test 4: Nested hash (Optional)
     */
    @Test
    public void testNestedHash() {
        RedisEmulator redis = new RedisEmulator();

        String nestedJson = "{\"city\":\"New York\",\"country\":\"USA\"}";

        redis.hset("user:1000", "profile", nestedJson, null);

        String actualProfile = redis.hget("user:1000", "profile");
        Assertions.assertEquals(nestedJson, actualProfile, "Test 4 Failed: Nested (JSON) hash not working as expected");
    }

    /**
     * Test 5: Memory limit and eviction policy
     */
    @Test
    public void testMemoryLimitAndEvictionPolicy() {
        RedisEmulator redis = new RedisEmulator(3);

        redis.set("key1", "value1", null);
        redis.set("key2", "value2", null);
        redis.set("key3", "value3", null);

        redis.set("key4", "value4", null);

        Assertions.assertNull(redis.get("key1"), "Test 5 Failed: LRU eviction not working");

        Assertions.assertEquals("value2", redis.get("key2"), "Test 5 Failed: Key should not be evicted incorrectly");
    }

    /**
     * Test 5: Persistence
     */
    @Test
    public void testPersistence() {
        RedisEmulator redis = new RedisEmulator(3);

        redis.set("key1", "value1", null);
        redis.set("key2", "value2", null);
        redis.set("key3", "value3", null);
        // should evict key1
        redis.set("key4", "value4", null);

        redis.saveToFile("test_dump.rdb");

        RedisEmulator newRedis = new RedisEmulator();

        newRedis.loadFromFile("test_dump.rdb");
        
        Assertions.assertEquals(
                "value2",
                newRedis.get("key2"),
                "Test 6 Failed: Persistence (load) not working"
        );

        Assertions.assertNull(
                newRedis.get("key1"),
                "Test 6 Failed: Persistence (eviction state) not maintained"
        );
    }
}
