package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters the targeted spell, then lets the controller cast a spell that shares a card type
 * with the countered spell from their hand without paying its mana cost.
 * Used by Counterlash.
 *
 * <p>If the targeted spell can't be countered (uncounterable), the controller may still cast
 * a spell from hand that shares a card type.</p>
 */
public record CounterlashEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}
