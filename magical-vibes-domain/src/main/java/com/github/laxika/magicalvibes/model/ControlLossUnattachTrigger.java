package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** A reflexive Equipment-unattach trigger registered for the rest of the current turn. */
public record ControlLossUnattachTrigger(UUID controllerId, Card sourceCard) {
}
