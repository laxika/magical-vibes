package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Counter target spell or ability unless its controller discards a card. Models the
 * "Ward—Discard a card" variant of the Ward ability (e.g. Forum Necroscribe). The affected
 * player chooses whether to discard a card (any card) or let their spell/ability be countered,
 * unless {@code random} makes the discard automatic and random.
 */
public record CounterUnlessDiscardsEffect(boolean random, CardPredicate discardPredicate) implements CounterUnlessEffect {

    /** The normal Ward form, where the affected player chooses the card to discard. */
    public CounterUnlessDiscardsEffect() {
        this(false, null);
    }

    /** The normal Ward form with a restriction on the discarded card. */
    public CounterUnlessDiscardsEffect(CardPredicate discardPredicate) {
        this(false, discardPredicate);
    }

    /** Compatibility constructor for the random-discard form. */
    public CounterUnlessDiscardsEffect(boolean random) {
        this(random, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.DISCARD_CARD;
    }

    @Override
    public int ransomMagnitude() {
        return 1;
    }
}
