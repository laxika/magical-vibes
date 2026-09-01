package com.github.laxika.magicalvibes.model;

/**
 * A mana payment component of a casting cost (e.g. "{G}" for Ancient Grudge's flashback).
 */
public record ManaCastingCost(String manaCost, boolean treasureManaOnly) implements CastingCost {

    public ManaCastingCost(String manaCost) {
        this(manaCost, false);
    }

    /** A mana cost that may be paid only with mana produced by Treasures. */
    public static ManaCastingCost treasureOnly(String manaCost) {
        return new ManaCastingCost(manaCost, true);
    }
}
