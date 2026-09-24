package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a card would be put into an opponent's graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Leyline of the Void.
 *
 * @param trackWithSource whether the exiled card is also tracked with the replacement's source
 * @param addVoidCounter whether the exiled card receives a void counter (Dauthi Voidwalker)
 */
public record ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource,
                                                          boolean addVoidCounter) implements CardEffect {

    public ExileOpponentCardsInsteadOfGraveyardEffect() {
        this(false, false);
    }

    public ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, false);
    }
}
