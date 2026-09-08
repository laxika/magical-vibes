package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed offer to cast a spell exiled by rebound at the beginning of its controller's next upkeep. */
public record ReboundAtNextUpkeep(UUID controllerId, UUID ownerId, Card card) implements DelayedAction {
}
