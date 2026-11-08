package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this token leaves the battlefield, return the exiled card to its owner's graveyard": returns the card linked to
 * the leaving token from exile to its owner's graveyard. The link is the token's chosen permanent id, read as the
 * ability resolves from the stack entry's source as it last existed ({@code StackEntry.getSourcePermanentSnapshot()}).
 */
public record ReturnLinkedCardToOwnerGraveyardEffect() implements CardEffect {
}
