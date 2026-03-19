package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect: if a creature matching the predicate you control would deal combat
 * damage to a player, instead that player mills that many cards.
 * Used by Undead Alchemist (Zombie subtype predicate).
 */
public record ReplaceCombatDamageWithMillEffect(
        PermanentPredicate attackerPredicate
) implements CardEffect {}
