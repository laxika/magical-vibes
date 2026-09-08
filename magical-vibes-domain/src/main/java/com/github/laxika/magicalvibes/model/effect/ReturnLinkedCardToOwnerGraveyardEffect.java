package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns the card linked to a leaving token from exile to its owner's graveyard.
 * The card ID is bound by the self-leaves trigger collector from the leaving permanent's link.
 */
public record ReturnLinkedCardToOwnerGraveyardEffect(UUID linkedCardId) implements CardEffect {

    public ReturnLinkedCardToOwnerGraveyardEffect() {
        this(null);
    }
}
