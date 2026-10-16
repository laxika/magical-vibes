package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Counters target spell, then searches that spell's controller's graveyard, hand, and library
 * for cards with the same name as that spell and exiles them. By default all matching cards are
 * exiled; optionally, the controller chooses any number. Then that player shuffles and, when
 * configured, draws a card for each card exiled from their hand this way.
 * <p>
 * If the target spell can't be countered (uncounterable or protected from the counter's color),
 * it stays on the stack but the search-and-exile still happens (per Counterbore's rulings).
 * <p>
 * <p>
 * When {@code exileCondition} is non-null only the search-and-exile half depends on it; the
 * counter is unconditional (Invasive Surgery's delirium).
 * <p>
 * Used by: Counterbore, Test of Talents, Invasive Surgery
 */
public record CounterSpellAndExileAllWithSameNameEffect(boolean chooseAnyNumber,
                                                         boolean drawCardsExiledFromHand, boolean chooseHiddenZoneMatches,
                                                         Condition exileCondition)
        implements CardEffect {

    public CounterSpellAndExileAllWithSameNameEffect(boolean chooseAnyNumber, boolean drawCardsExiledFromHand,
                                                     boolean chooseHiddenZoneMatches) {
        this(chooseAnyNumber, drawCardsExiledFromHand, chooseHiddenZoneMatches, null);
    }

    public CounterSpellAndExileAllWithSameNameEffect(boolean chooseAnyNumber, boolean drawCardsExiledFromHand) {
        this(chooseAnyNumber, drawCardsExiledFromHand, false);
    }

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
