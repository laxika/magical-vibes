package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Tracks the controller and source card for an exile-linked control-loss trigger. */
public record ExiledCardsControlLossWatch(UUID controllerId, Card sourceCard) {
}
