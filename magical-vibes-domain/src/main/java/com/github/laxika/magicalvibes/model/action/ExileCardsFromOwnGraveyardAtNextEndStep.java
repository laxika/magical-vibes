package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that exiles cards from its controller's graveyard at the next end step. */
public record ExileCardsFromOwnGraveyardAtNextEndStep(
        UUID controllerId, int count, Card sourceCard) implements DelayedAction {
}
