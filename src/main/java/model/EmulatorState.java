package model;

import java.util.*;

public class EmulatorState {
    private Map<String, StringWrapper> stringStorage;
    private Map<String, HashWrapper> hashStorage;
    // Order of LRU keys (least -> most recently used)
    private List<String> lruKeys;

    private int maxMemory;
    private int currentSize;

    public EmulatorState(int maxMemory) {
        this.stringStorage = new HashMap<>();
        this.hashStorage = new HashMap<>();
        this.lruKeys = new LinkedList<>();
        this.maxMemory = maxMemory;
        this.currentSize = 0;
    }


    public EmulatorState() {
        this(Integer.MAX_VALUE);
    }

    public EmulatorState(Map<String, StringWrapper> stringStorage, Map<String, HashWrapper> hashStorage, List<String> lruKeys, int maxMemory, int currentSize) {
        this.stringStorage = stringStorage;
        this.hashStorage = hashStorage;
        this.lruKeys = lruKeys;
        this.maxMemory = maxMemory;
        this.currentSize = currentSize;
    }

    public Map<String, StringWrapper> getStringStorage() {
        return stringStorage;
    }

    public void setStringStorage(Map<String, StringWrapper> stringStorage) {
        this.stringStorage = stringStorage;
    }

    public Map<String, HashWrapper> getHashStorage() {
        return hashStorage;
    }

    public void setHashStorage(Map<String, HashWrapper> hashStorage) {
        this.hashStorage = hashStorage;
    }

    public List<String> getLruKeys() {
        return lruKeys;
    }

    public void setLruKeys(List<String> lruKeys) {
        this.lruKeys = lruKeys;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmulatorState that)) return false;
        return getMaxMemory() == that.getMaxMemory() && getCurrentSize() == that.getCurrentSize() && getStringStorage().equals(that.getStringStorage()) && getHashStorage().equals(that.getHashStorage()) && getLruKeys().equals(that.getLruKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStringStorage(), getHashStorage(), getLruKeys(), getMaxMemory(), getCurrentSize());
    }

    @Override
    public String toString() {
        return "EmulatorState{" +
                "stringStorage=" + stringStorage +
                ", hashStorage=" + hashStorage +
                ", lruKeys=" + lruKeys +
                ", maxMemory=" + maxMemory +
                ", currentSize=" + currentSize +
                '}';
    }
}
