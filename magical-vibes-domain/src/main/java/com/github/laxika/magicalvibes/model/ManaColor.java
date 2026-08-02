package com.github.laxika.magicalvibes.model;

import java.util.List;

public enum ManaColor {

    WHITE("W"),
    BLUE("U"),
    BLACK("B"),
    RED("R"),
    GREEN("G"),
    COLORLESS("C");

    /**
     * The five colors a "of any color" choice ranges over. CR 105.4: colorless is not a color, so
     * an any-color mana source can never pay a {@code {C}} pip and never counts as covering one.
     */
    public static final List<ManaColor> COLORS = List.of(WHITE, BLUE, BLACK, RED, GREEN);

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
