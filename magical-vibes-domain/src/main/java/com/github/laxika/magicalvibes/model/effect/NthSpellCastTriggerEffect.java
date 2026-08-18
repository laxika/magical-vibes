package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CountScope;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast your Nth spell in a turn" abilities.
 * <p>
 * Works in {@code ON_CONTROLLER_CASTS_SPELL} and {@code ON_OPPONENT_CASTS_SPELL} slots for the
 * relevant caster's count, or in
 * {@code ON_ANY_PLAYER_CASTS_SPELL} slot with {@link CountScope#ANY_PLAYER} for the total count
 * across all players. The trigger fires only when the selected spell-cast count for the current
 * turn equals {@code spellNumber}.
 * <p>
 * When wrapped in {@link MayEffect}, the player is prompted before the resolved
 * effects execute (e.g. "you may transform" on Vance's Blasting Cannons).
 *
 * @param spellNumber     the exact spell number that triggers this (e.g. 3 for "third spell")
 * @param resolvedEffects effects to put on the stack when this triggers
 * @param countScope      whose spell-cast count to use
 */
public record NthSpellCastTriggerEffect(
        int spellNumber,
        List<CardEffect> resolvedEffects,
        CountScope countScope
) implements CardEffect {

    public NthSpellCastTriggerEffect(int spellNumber, List<CardEffect> resolvedEffects) {
        this(spellNumber, resolvedEffects, CountScope.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null,
                resolvedEffects.stream().anyMatch(e -> e.targetSpec().selfTargeting()), 1);
    }
}
