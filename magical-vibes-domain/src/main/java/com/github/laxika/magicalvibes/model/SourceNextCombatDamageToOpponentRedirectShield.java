package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A one-shot redirect shield attached to a <em>damage source</em>: the next time
 * {@code sourcePermanentId} would deal combat damage to an opponent of its controller this turn,
 * that damage is dealt to {@code destinationPermanentId} instead and the shield is consumed. Used
 * by Soltari Guerrillas.
 *
 * <p>Unlike the other redirect shields this one is keyed on who <em>deals</em> the damage rather
 * than who receives it, and it only matches combat damage dealt to a player (damage to a
 * planeswalker or to a creature is left alone). Cleared at turn cleanup.</p>
 *
 * @param sourcePermanentId      the permanent whose next combat damage to an opponent is redirected
 * @param destinationPermanentId the creature the redirected damage is dealt to instead
 */
public record SourceNextCombatDamageToOpponentRedirectShield(
        UUID sourcePermanentId,
        UUID destinationPermanentId
) {
}
