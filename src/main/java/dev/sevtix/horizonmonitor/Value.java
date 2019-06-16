package dev.sevtix.horizonmonitor;

public class Value {

    private String title, description, value;
    private ValueType type;

    public Value() {
    }

    public Value(String title, String description, String value, ValueType type) {
        this.title = title;
        this.description = description;
        this.value = value;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }
}
