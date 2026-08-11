package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * Delayed trigger watching one chosen creature for damage until the end of the turn.
 * The resulting ability is controlled by {@code controllerId}, the player who registered it.
 */
public record DelayedWatchedCreatureDealsDamage(
        UUID watchedPermanentId,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard
) implements DelayedAction {
}
