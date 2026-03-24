package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record PendingExileReturn(Card card, UUID controllerId, boolean returnTapped, boolean returnToHand) {
    public PendingExileReturn(Card card, UUID controllerId) {
        this(card, controllerId, false, false);
    }

    public PendingExileReturn(Card card, UUID controllerId, boolean returnTapped) {
        this(card, controllerId, returnTapped, false);
    }
}
