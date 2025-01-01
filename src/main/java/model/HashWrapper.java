package model;

import java.util.Map;
import java.util.Objects;

public class HashWrapper {
    private Map<String, StringWrapper> fields;

    public HashWrapper(Map<String, StringWrapper> fields) {
        this.fields = fields;
    }

    public Map<String, StringWrapper> getFields() {
        return fields;
    }

    public void setFields(Map<String, StringWrapper> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashWrapper)) return false;
        HashWrapper that = (HashWrapper) o;
        return getFields().equals(that.getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFields());
    }

    @Override
    public String toString() {
        return "HashWrapper{" +
                "fields=" + fields +
                '}';
    }
}
