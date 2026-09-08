package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time choice to create a token copy of each selected artifact or creature token
 * controlled by the effect's controller. The selected tokens must have different names.
 */
public record CreateTokenCopiesOfChosenDistinctControlledTokensEffect() implements CardEffect {
}
