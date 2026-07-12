package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters target spell, then searches that spell's controller's graveyard, hand, and library
 * for all cards with the same name as that spell and exiles them. Then that player shuffles.
 * <p>
 * If the target spell can't be countered (uncounterable or protected from the counter's color),
 * it stays on the stack but the search-and-exile still happens (per Counterbore's rulings).
 * <p>
 * Used by: Counterbore
 */
public record CounterSpellAndExileAllWithSameNameEffect() implements CardEffect {

    @Override
    public boolean canTargetSpell() {
        return true;
    }
}
