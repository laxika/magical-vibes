package com.github.laxika.magicalvibes.model;

public enum DeckFormat {
    CASUAL, STANDARD, PIONEER, MODERN, LEGACY, VINTAGE, PAUPER, COMMANDER;

    public int startingLife() { return this == COMMANDER ? 40 : 20; }
}
