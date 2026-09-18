package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect for Lich: its controller sacrifices that many nontoken permanents, or loses
 * the game if that many cannot be sacrificed.
 */
public record SacrificeNontokenPermanentsOrLoseGameEffect() implements CardEffect {

    @Override
    public boolean referencesEventValue() {
        return true;
    }
}
