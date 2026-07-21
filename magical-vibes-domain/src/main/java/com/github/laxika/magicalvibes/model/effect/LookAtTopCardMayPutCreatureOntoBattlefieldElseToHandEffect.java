package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top card of the controller's library. If it's a creature card, the controller may put
 * it onto the battlefield. If they don't (decline, or the card isn't a creature), put it into their
 * hand.
 *
 * <p>Used by Believe (Reason // Believe aftermath half). "Look at" is private — the card's identity
 * is not broadcast to opponents. The optional creature→battlefield choice runs via a paired mayfx
 * handler.
 */
public record LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect() implements CardEffect {
}
