package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "[This creature] attacks this turn (or this combat) if able", made by a resolving spell or ability: the payload of a
 * floating effect on the creature, whose {@link Duration} says whether it lasts the turn or the combat. Each one is
 * its own requirement (CR 508.1d).
 *
 * @param defenderId the player the creature must attack, not that player's planeswalkers ("it attacks you this turn if
 *                   able"), or {@code null} when any player or planeswalker will do
 */
public record MustAttackIfAbleEffect(UUID defenderId) implements CardEffect {

    /** A requirement to attack any player or planeswalker. */
    public MustAttackIfAbleEffect() {
        this(null);
    }
}
