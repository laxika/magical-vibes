package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB effect: the controller exiles the top N cards of their own library,
 * tracked as "exiled with" the source permanent (e.g. Colfenor's Plans).
 *
 * <p>Unlike {@link EachPlayerExilesTopCardsToSourceEffect} (which affects every player),
 * this only exiles the controller's library. Pair with
 * {@link AllowCastFromCardsExiledWithSourceEffect} to let the controller play those cards.
 *
 * <p>{@code faceDown} controls CR 406.3 visibility: Colfenor's Plans exiles face down, while Search
 * the City exiles face up (its own trigger asks players to compare names against those cards).
 *
 * <p>{@code toGraveyardOnControlLoss} registers the source permanent for the control-loss watch
 * ({@code GameData.exiledCardsToGraveyardOnControlLossWatch}), so "when you lose control of this
 * permanent, put all cards exiled with it into their owner's graveyard" is honoured when control
 * changes or it leaves the battlefield. Used by Duplicity; see
 * {@link ExileCardFromHandFaceDownWithSourceEffect} for the same flag on the hand-exile variant.
 */
public record ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss)
        implements CardEffect {

    /** Face-down exile (Colfenor's Plans). */
    public ExileTopCardsToSourceEffect(int count) {
        this(count, true, false);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown) {
        this(count, faceDown, false);
    }
}
