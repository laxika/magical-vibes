package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell or ability unless its controller discards a card or pays generic mana.
 * Used for Ward variants that offer both options as one ransom.
 */
public record CounterUnlessDiscardsOrPaysEffect(int amount)
        implements CounterSpellingEffect, CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.DISCARD_CARD_OR_PAY_MANA;
    }

    @Override
    public int ransomMagnitude() {
        return amount;
    }
}
