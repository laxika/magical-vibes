package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/**
 * Delayed trigger that makes its controller discard the cards sought by one seek at the beginning of
 * their controller's next end step (Phyrexian Harvester: "At the beginning of your next end step,
 * discard those cards"). One instance covers every card that a single seek put into hand.
 */
public record DiscardSpecificCardAtNextEndStep(UUID controllerId, List<UUID> cardIds, Card sourceCard)
        implements DelayedAction {
}
