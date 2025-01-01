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
}
