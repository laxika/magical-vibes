package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that makes its controller discard cards at the beginning of the next end step. */
public record DiscardCardsAtNextEndStep(UUID controllerId, int count, Card sourceCard)
        implements DelayedAction {
}
