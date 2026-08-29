package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** A recurring Epic trigger that creates a copy of {@code spellPrototype} at the controller's upkeep. */
public record EpicDelayedTrigger(UUID controllerId, Card spellPrototype, UUID targetId) implements DelayedAction {
}
