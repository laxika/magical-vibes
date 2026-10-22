package com.github.laxika.magicalvibes.model;

public enum DeckFormat {
    CASUAL, STANDARD, PIONEER, MODERN, LEGACY, VINTAGE, PAUPER, COMMANDER;

    public int startingLife() { return this == COMMANDER ? 40 : 20; }

    /**
     * The minimum deck size for the format: 40 for the casual/limited default, 100 for Commander,
     * and 60 for the other constructed formats.
     */
    public int minimumDeckSize() {
        return switch (this) {
            case CASUAL -> 40;
            case COMMANDER -> 100;
            default -> 60;
        };
    }
}
