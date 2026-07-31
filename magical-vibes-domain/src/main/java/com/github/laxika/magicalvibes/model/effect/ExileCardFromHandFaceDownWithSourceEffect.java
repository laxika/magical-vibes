package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller exiles a card of their choice from their hand face down (CR 406.3), tracked "with"
 * the source permanent via {@code GameData.exiledCards} / {@code sourcePermanentId}. Only the source
 * permanent's controller sees the exiled card, which models "You may look at it for as long as it
 * remains exiled".
 *
 * <p>{@code toGraveyardOnControlLoss} additionally registers the source permanent for the
 * control-loss watch ({@code GameData.exiledCardsToGraveyardOnControlLossWatch}): when its
 * controller changes or it leaves the battlefield, every card exiled with it is put into its
 * owner's graveyard. Used by Gustha's Scepter.
 *
 * <p>Companion to {@link PutCardExiledWithSourceIntoHandEffect}, which takes such a card back.
 */
public record ExileCardFromHandFaceDownWithSourceEffect(boolean toGraveyardOnControlLoss) implements CardEffect {
}
