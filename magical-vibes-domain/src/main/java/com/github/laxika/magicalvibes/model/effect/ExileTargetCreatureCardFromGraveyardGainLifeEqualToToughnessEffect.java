package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile target creature card from a graveyard. You gain life equal to that card's toughness."
 *
 * <p>The single graveyard target (any graveyard) has its printed toughness captured before it is
 * exiled, so the sizing card is consumed by the same effect that reads it — the reason this is not
 * decomposed into an exile effect plus a toughness {@code DynamicAmount}. Null or negative toughness
 * clamps to 0. If the target has left the graveyard the ability fizzles and no life is gained.
 *
 * <p>The life-gain analog of {@link ExileTargetCreatureCardCreateTokensEqualToToughnessEffect}.
 * Used by Rotfeaster Maggot.
 */
public record ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD);
    }
}
