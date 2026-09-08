package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed trigger that returns an exiled card at the next end step unless its controller pays. */
public record ReturnExiledCardAtNextEndStepUnlessPays(
        UUID cardId,
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId
) implements DelayedAction {
}
