package com.github.laxika.magicalvibes.model;

public enum ManaColor {

    WHITE("W"),
    BLUE("U"),
    BLACK("B"),
    RED("R"),
    GREEN("G");

    private final String code;

    ManaColor(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ManaColor fromCode(String code) {
        for (ManaColor color : values()) {
            if (color.code.equals(code)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown mana color code: " + code);
    }
}
