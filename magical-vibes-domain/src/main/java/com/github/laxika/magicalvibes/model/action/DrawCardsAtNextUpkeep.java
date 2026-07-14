package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed trigger: {@code controllerId} draws {@code count} cards at the beginning of the next
 *  turn's upkeep (e.g. Library of Lat-Nam's first mode). Drained in {@code StepTriggerService}. */
public record DrawCardsAtNextUpkeep(UUID controllerId, int count, Card sourceCard) implements DelayedAction {
}
