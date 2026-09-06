package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * Delayed trigger watching one Wall for damage dealt by attacking creatures until end of turn.
 */
public record DelayedWatchedCreatureDealtDamageByAttackingCreature(
        UUID watchedPermanentId,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard
) implements DelayedAction {
}
