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
 * trigger) and covers combat damage to anything: a player, a planeswalker, or another creature,
 * unless {@code combatDamageToPlayerOnly} is set. It expires during cleanup when
 * {@code untilEndOfTurn} is set; otherwise it expires at the controller's next turn.
 *
 * <p>Cleared at the beginning of {@code controllerId}'s next turn by {@code TurnProgressionService},
 * matching the "until your next turn" duration rather than the usual end-of-turn cleanup.
 *
 * <p>{@code creatingPermanentId} is the permanent whose ability created this delayed triggered ability (CR 603.7), or
 * {@code null}: the stack entry of each trigger names it as its granting permanent, which "convert Optimus Prime"
 * reads ({@code TransformGrantingPermanentEffect}). The ability is the watching player's, not the watched creature's,
 * so nothing that adds triggers for that creature's abilities applies to it.
 */
public record DelayedWatchedCreaturesCombatDamage(
        Set<UUID> watchedPermanentIds,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard,
        boolean combatDamageToPlayerOnly,
        boolean untilEndOfTurn,
        UUID creatingPermanentId
) implements DelayedAction {

    /** A watch that names no creating permanent. */
    public DelayedWatchedCreaturesCombatDamage(Set<UUID> watchedPermanentIds, UUID controllerId,
                                               List<CardEffect> effects, Card sourceCard,
                                               boolean combatDamageToPlayerOnly, boolean untilEndOfTurn) {
        this(watchedPermanentIds, controllerId, effects, sourceCard, combatDamageToPlayerOnly, untilEndOfTurn, null);
    }
}
