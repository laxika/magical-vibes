package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the spell was kicked.
 * (MTG Rule 702.32c — "If [this spell] was kicked, [effect].")
 *
 * @param wrapped the inner effect to resolve when the spell was kicked
 */
public record KickedConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "kicked";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was not kicked";
    }
}
