package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a card would be put into an opponent's graveyard
 * from anywhere, exile it instead (CR 614.1). Used by Leyline of the Void and
 * Dauthi Voidwalker. When {@code onlyCardsYouDidntControl} is set, the replacement
 * applies only to cards its source's controller did not control, as Valgavoth does.
 *
 * @param addVoidCounter whether exiled cards receive a void counter
 * @param onlyCardsYouDidntControl whether cards controlled by the source's controller are excluded
 */
public record ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource,
                                                          boolean addVoidCounter,
                                                          boolean onlyCardsYouDidntControl) implements CardEffect {

    public ExileOpponentCardsInsteadOfGraveyardEffect() {
        this(false, false, false);
    }

    public ExileOpponentCardsInsteadOfGraveyardEffect(boolean trackWithSource) {
        this(trackWithSource, false, false);
    }

    public static ExileOpponentCardsInsteadOfGraveyardEffect withVoidCounter() {
        return new ExileOpponentCardsInsteadOfGraveyardEffect(false, true, false);
    }

    public static ExileOpponentCardsInsteadOfGraveyardEffect forCardsYouDidntControl(boolean trackWithSource) {
        return new ExileOpponentCardsInsteadOfGraveyardEffect(trackWithSource, false, true);
    }
}
