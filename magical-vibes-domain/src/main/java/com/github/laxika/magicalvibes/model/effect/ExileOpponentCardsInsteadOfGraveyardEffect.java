package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static replacement effect: if a card would be put into an opponent's graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Leyline of the Void.
 */
public record ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource, CounterType counterType)
        implements CardEffect {

    public ExileOpponentCardsInsteadOfGraveyardEffect() {
        this(false, null);
    }

    public ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, null);
    }

    public static ExileOpponentCardsInsteadOfGraveyardEffect withVoidCounter() {
        return new ExileOpponentCardsInsteadOfGraveyardEffect(false, CounterType.VOID);
    }
}
