package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

import java.util.UUID;

/** Delayed trigger that creates a token copy of an artifact at the next end step. */
public record DelayedCreateTokenCopy(UUID controllerId, Card sourceCard, Card copiedCard, CreateTokenCopyOfSourceEffect copyEffect)
        implements DelayedAction {
    public DelayedCreateTokenCopy(UUID controllerId, CreateTokenCopyOfSourceEffect copyEffect, Card sourceCard) {
        this(controllerId, sourceCard, sourceCard, copyEffect);
    }
        public DelayedCreateTokenCopy(UUID controllerId, Card sourceCard, Card copiedCard) {
            this(controllerId, sourceCard, copiedCard, null);
        }

}
