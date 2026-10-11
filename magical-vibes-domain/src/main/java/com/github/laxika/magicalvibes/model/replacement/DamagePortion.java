package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * The part of a combat damage event dealt by one source. All combat damage of a step is dealt
 * simultaneously (CR 510.2), so everything one recipient is dealt forms one event, and effects that
 * care about the source ("if a source would deal damage to you, prevent 1 of that damage") act on
 * that source's portion.
 *
 * @param sourceId          the permanent dealing this portion
 * @param sourceSnapshot    its state when combat damage was assigned
 * @param amount            the damage of this portion still to be dealt
 * @param counterDamage     the source deals this damage as counters or poison (infect, wither)
 * @param deathtouch        the source has deathtouch
 * @param unblocked         the source is an unblocked attacker
 * @param unpreventable     this source's damage can't be prevented
 */
public record DamagePortion(UUID sourceId, Permanent sourceSnapshot, int amount, boolean counterDamage,
                            boolean deathtouch, boolean unblocked, boolean unpreventable) {

    public DamagePortion withAmount(int newAmount) {
        return new DamagePortion(sourceId, sourceSnapshot, Math.max(0, newAmount), counterDamage, deathtouch,
                unblocked, unpreventable);
    }

    public DamagePortion copyForSimulation() {
        return new DamagePortion(sourceId, sourceSnapshot == null ? null : new Permanent(sourceSnapshot), amount,
                counterDamage, deathtouch, unblocked, unpreventable);
    }
}
