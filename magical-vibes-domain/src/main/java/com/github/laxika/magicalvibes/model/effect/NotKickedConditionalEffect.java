package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the spell was NOT kicked.
 * (MTG Rule 702.32 — "if [this permanent] wasn't kicked, [effect].")
 *
 * @param wrapped the inner effect to resolve when the spell was not kicked
 */
public record NotKickedConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "wasn't kicked";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was kicked";
    }
}
