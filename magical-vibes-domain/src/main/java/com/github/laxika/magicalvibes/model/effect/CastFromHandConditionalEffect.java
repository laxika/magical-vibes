package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the permanent was cast from hand.
 * (MTG intervening-if clause — CR 603.4: "When this creature enters, if you cast it from your hand, [effect].")
 *
 * @param wrapped the inner effect to resolve when the permanent was cast from hand
 */
public record CastFromHandConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "cast from hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "permanent was not cast from hand";
    }
}
