package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** An effect whose card reference is supplied by a card chosen during activation-cost payment. */
public interface ActivationCostCardReferenceEffect extends CardEffect {

    CardEffect bindToCard(UUID cardId);

    /**
     * Binds the paid card and, when needed, snapshots a value derived from that card at activation
     * time. Effects with no such value can keep the ordinary binding behavior.
     */
    default CardEffect bindToCard(UUID cardId, int paidCardManaValue) {
        return bindToCard(cardId);
    }
}
