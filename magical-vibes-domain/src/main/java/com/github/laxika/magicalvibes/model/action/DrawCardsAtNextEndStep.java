package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that makes a player draw cards at the beginning of the next end step. */
public record DrawCardsAtNextEndStep(UUID controllerId, int count, Card sourceCard)
        implements DelayedAction {
}
