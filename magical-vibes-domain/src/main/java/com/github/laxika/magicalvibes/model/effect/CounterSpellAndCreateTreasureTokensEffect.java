package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters target spell and creates Treasure tokens equal to that spell's mana value.
 *
 * <p>If the target spell cannot be countered (e.g. uncounterable), the counter part does nothing
 * but Treasure tokens are still created based on the spell's mana value (the spell resolves
 * as much as possible per MTG rules).</p>
 *
 * <p>Used by Spell Swindle.</p>
 */
public record CounterSpellAndCreateTreasureTokensEffect() implements CardEffect {
    @Override public boolean canTargetSpell() { return true; }
}
