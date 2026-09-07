package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect: if a creature matching the predicate you control would deal combat
 * damage to a player, instead that player mills that many cards and, when {@code counterType} is
 * non-null, that many counters of that type are put on the attacker.
 * Used by Undead Alchemist (Zombie subtype predicate) and Szadek, Lord of Secrets (source-card
 * predicate with +1/+1 counters).
 */
public record ReplaceCombatDamageWithMillEffect(
        PermanentPredicate attackerPredicate,
        CounterType counterType
) implements CardEffect {

    public ReplaceCombatDamageWithMillEffect(PermanentPredicate attackerPredicate) {
        this(attackerPredicate, null);
    }
}
