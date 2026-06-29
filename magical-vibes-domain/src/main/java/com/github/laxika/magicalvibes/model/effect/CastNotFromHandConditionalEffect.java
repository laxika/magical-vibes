package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the spell was cast from
 * anywhere other than the controller's hand (e.g. flashback from the graveyard).
 *
 * @param wrapped the inner effect to resolve when the spell was not cast from hand
 */
public record CastNotFromHandConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "cast from anywhere other than your hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was cast from hand";
    }
}
