package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/**
 * Delayed trigger that deals damage for the cards from one effect resolution that remain exiled.
 */
public record DamageForCardsStillExiledAtNextEndStep(
        UUID controllerId,
        UUID sourcePermanentId,
        Card sourceCard,
        List<UUID> cardIds,
        int damagePerCard) implements DelayedAction {
}
