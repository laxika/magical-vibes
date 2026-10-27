package com.github.laxika.magicalvibes.model.effect;

/**
 * Static restriction: this creature can't attack its card owner. When {@code includePlaneswalkers} is
 * true it also can't attack planeswalkers that player controls (Xantcha, Sleeper Agent); Alexios, Deimos
 * of Kosmos and Elrond's granted restriction name only the owner as a player.
 */
public record CantAttackCardOwnerEffect(boolean includePlaneswalkers) implements CardEffect {

    public CantAttackCardOwnerEffect() {
        this(true);
    }
}
