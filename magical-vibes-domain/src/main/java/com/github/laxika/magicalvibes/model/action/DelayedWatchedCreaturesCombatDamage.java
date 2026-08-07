package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Delayed trigger: "Until your next turn, whenever either of those creatures deals combat damage,
 * &lt;effects&gt;." Registered by Tamiyo, Field Researcher's +1 against the creatures it chose.
 *
 * <p>The watch is keyed by permanent id, so a watched creature that changes controller keeps
 * triggering, and the resulting ability is always controlled by {@code controllerId} — the player
 * who activated the ability — regardless of who controls the damaging creature. It fires once per
 * watched creature per combat damage step (all damage that creature deals simultaneously is one
 * trigger) and covers combat damage to anything: a player, a planeswalker, or another creature.
 *
 * <p>Cleared at the beginning of {@code controllerId}'s next turn by {@code TurnProgressionService},
 * matching the "until your next turn" duration rather than the usual end-of-turn cleanup.
 */
public record DelayedWatchedCreaturesCombatDamage(
        Set<UUID> watchedPermanentIds,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard
) implements DelayedAction {
}
