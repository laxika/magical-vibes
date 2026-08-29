package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that destroys all permanents at the beginning of the next end step. */
public record DelayedDestroyAllPermanents(UUID controllerId, Card sourceCard) implements DelayedAction {
}
