package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for nontoken creatures an opponent owns and creature cards an
 * opponent owns outside the battlefield.
 */
public record ExileOpponentCreatureCardsInsteadOfGraveyardEffect()
        implements OpponentCreatureCardExileReplacement {
}
