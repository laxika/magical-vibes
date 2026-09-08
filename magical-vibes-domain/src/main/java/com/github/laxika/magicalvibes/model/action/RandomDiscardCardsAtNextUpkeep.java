package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that makes its controller discard cards at random at the beginning of their next upkeep. */
public record RandomDiscardCardsAtNextUpkeep(UUID controllerId, int count, Card sourceCard)
        implements DelayedAction {
}
