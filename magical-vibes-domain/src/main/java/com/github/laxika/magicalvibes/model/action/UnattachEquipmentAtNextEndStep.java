package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that unattaches an Equipment at the beginning of the next end step. */
public record UnattachEquipmentAtNextEndStep(UUID controllerId, UUID equipmentId, Card sourceCard)
        implements DelayedAction {
}
