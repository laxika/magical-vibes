package com.github.laxika.magicalvibes.model;

public enum ManaColor {

    WHITE("W"),
    BLUE("U"),
    BLACK("B"),
    RED("R"),
    GREEN("G"),
    COLORLESS("C");

    private final String code;

    ManaColor(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Returns the ManaColor for the given code, or {@code null} if the code
     * does not match any color (e.g. generic mana like "2").
     */
    public static ManaColor fromCode(String code) {
        return switch (code) {
            case "W" -> WHITE;
            case "U" -> BLUE;
            case "B" -> BLACK;
            case "R" -> RED;
            case "G" -> GREEN;
            case "C" -> COLORLESS;
            default -> null;
        };
    }
}
