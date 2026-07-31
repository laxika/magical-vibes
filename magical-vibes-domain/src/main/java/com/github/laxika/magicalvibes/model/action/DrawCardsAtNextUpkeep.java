package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed trigger: {@code controllerId} draws {@code count} cards at the beginning of the next
 *  turn's upkeep (e.g. Library of Lat-Nam's first mode). With {@code upTo} the player instead
 *  chooses any number from 0 to {@code count} through a delayed triggered ability on the stack
 *  (Arcane Denial). Drained in {@code StepTriggerService}. */
public record DrawCardsAtNextUpkeep(UUID controllerId, int count, Card sourceCard, boolean upTo)
        implements DelayedAction {

    public DrawCardsAtNextUpkeep(UUID controllerId, int count, Card sourceCard) {
        this(controllerId, count, sourceCard, false);
    }
}
