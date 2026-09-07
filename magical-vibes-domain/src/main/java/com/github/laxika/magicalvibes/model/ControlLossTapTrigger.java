package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** A delayed tap trigger registered for a permanent until its controller loses control of it. */
public record ControlLossTapTrigger(UUID controllerId, Card sourceCard) {
}
