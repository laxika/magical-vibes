package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that draws one card for each player dealt combat damage this turn. */
public record DrawCardsAtNextMainPhase(UUID controllerId, Card sourceCard) implements DelayedAction {
}
