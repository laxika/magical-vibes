package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell or ability unless its controller discards a card. Models the
 * "Ward—Discard a card" variant of the Ward ability (e.g. Forum Necroscribe). The affected
 * player chooses whether to discard a card (any card) or let their spell/ability be countered.
 */
public record CounterUnlessDiscardsEffect() implements CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.SPELL_ON_STACK);
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
