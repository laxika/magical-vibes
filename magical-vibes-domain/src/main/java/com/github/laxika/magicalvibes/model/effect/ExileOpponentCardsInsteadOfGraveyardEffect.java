package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a card would be put into an opponent's graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Leyline of the Void and
 * Dauthi Voidwalker.
 *
 * @param addVoidCounter whether exiled cards receive a void counter
 */
public record ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource,
                                                          boolean addVoidCounter) implements CardEffect {

    public ExileOpponentCardsInsteadOfGraveyardEffect() {
        this(false, false);
    }

    public ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, false);
    }

    public static ExileOpponentCardsInsteadOfGraveyardEffect withVoidCounter() {
        return new ExileOpponentCardsInsteadOfGraveyardEffect(false, true);
    }
}
