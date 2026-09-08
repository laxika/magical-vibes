package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters target spell, then searches that spell's controller's graveyard, hand, and library
 * for cards with the same name as that spell and exiles them. By default all matching cards are
 * exiled; optionally, the controller chooses any number. Then that player shuffles and, when
 * configured, draws a card for each card exiled from their hand this way.
 * <p>
 * If the target spell can't be countered (uncounterable or protected from the counter's color),
 * it stays on the stack but the search-and-exile still happens (per Counterbore's rulings).
 * <p>
 * Used by: Counterbore, Test of Talents
 */
public record CounterSpellAndExileAllWithSameNameEffect(boolean chooseAnyNumber,
                                                         boolean drawCardsExiledFromHand)
        implements CardEffect {

    public CounterSpellAndExileAllWithSameNameEffect() {
        this(false, false);
    }

    public CounterSpellAndExileAllWithSameNameEffect(boolean drawCardsExiledFromHand) {
        this(false, drawCardsExiledFromHand);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
