package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast your Nth spell in a turn" abilities.
 * <p>
 * Works in {@code ON_CONTROLLER_CASTS_SPELL} slot. The trigger fires only when
 * the controller's spell-cast count for the current turn equals {@code spellNumber}.
 * <p>
 * When wrapped in {@link MayEffect}, the player is prompted before the resolved
 * effects execute (e.g. "you may transform" on Vance's Blasting Cannons).
 *
 * @param spellNumber     the exact spell number that triggers this (e.g. 3 for "third spell")
 * @param resolvedEffects effects to put on the stack when this triggers
 */
public record NthSpellCastTriggerEffect(
        int spellNumber,
        List<CardEffect> resolvedEffects
) implements CardEffect {

    @Override
    public boolean isSelfTargeting() {
        return resolvedEffects.stream().anyMatch(CardEffect::isSelfTargeting);
    }
}
