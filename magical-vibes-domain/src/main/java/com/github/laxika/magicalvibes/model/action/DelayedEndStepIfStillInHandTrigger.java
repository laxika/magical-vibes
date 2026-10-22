package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * A next-end-step trigger with the intervening "if that card is in your hand" condition (CR 603.4).
 * It triggers only if the recorded card is still in its controller's hand as the same hand entry
 * (identified by {@code handEntryVersion}), since a card that leaves and returns is a new object (CR 400.7).
 */
public record DelayedEndStepIfStillInHandTrigger(
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId,
        UUID cardId,
        long handEntryVersion,
        CardEffect effect
) implements DelayedAction {
}
