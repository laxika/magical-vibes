package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record PendingExileReturn(Card card, UUID controllerId, boolean returnTapped) {
    public PendingExileReturn(Card card, UUID controllerId) {
        this(card, controllerId, false);
    }
}
