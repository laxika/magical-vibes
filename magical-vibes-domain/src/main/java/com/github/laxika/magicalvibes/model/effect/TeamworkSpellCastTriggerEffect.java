package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast a spell using teamwork" abilities.
 *
 * <p>Placed in {@code ON_CONTROLLER_CASTS_SPELL}; the spell-cast collector checks the triggering
 * spell's recorded teamwork-cost payment before reusing the generic spell-cast trigger plumbing.</p>
 */
public record TeamworkSpellCastTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {
}
