package model;

import java.util.Objects;

public class StringWrapper {
    private String value;
    private Long ttl;

    public StringWrapper() {
        this.value = null;
        this.ttl = null;
    }

    public StringWrapper(String value) {
        this.value = value;
        this.ttl = null;
    }

    public StringWrapper(String value, Long ttl) {
        this.value = value;
        this.ttl = ttl;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringWrapper)) return false;
        StringWrapper that = (StringWrapper) o;
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getTtl(), that.getTtl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getTtl());
    }

    @Override
    public String toString() {
        return "StringWrapper{" +
                "value='" + value + '\'' +
                ", ttl=" + ttl +
                '}';
    }
}
