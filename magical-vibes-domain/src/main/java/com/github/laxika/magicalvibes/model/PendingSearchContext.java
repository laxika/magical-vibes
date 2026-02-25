package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * Stores context needed to re-invoke a library search handler after a Leonin Arbiter
 * tax payment is accepted via the MayAbility system.
 *
 * @param originalEntry     the resolved StackEntry whose search effect was paused
 * @param searchEffect      the specific search CardEffect to re-dispatch
 * @param unpaidArbiterIds  permanent IDs of Leonin Arbiters whose tax is being paid
 */
public record PendingSearchContext(StackEntry originalEntry, CardEffect searchEffect, List<UUID> unpaidArbiterIds) {
}
