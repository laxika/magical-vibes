package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell or ability unless its controller discards a card. Models the
 * "Ward—Discard a card" variant of the Ward ability (e.g. Forum Necroscribe). The affected
 * player chooses whether to discard a card (any card) or let their spell/ability be countered.
 */
public record CounterUnlessDiscardsEffect() implements CardEffect {

    @Override
    public boolean canTargetSpell() {
        return true;
    }
}
