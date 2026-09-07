package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that creates a token copy of an artifact at the next end step. */
public record DelayedCreateTokenCopy(UUID controllerId, Card sourceCard, Card copiedCard)
        implements DelayedAction {
}
