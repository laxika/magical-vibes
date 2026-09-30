package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Registers a delayed trigger watching the player chosen as the enclosing ability's target.
 * This is the targeted counterpart to {@link RegisterDelayedControllerSpellCastTriggerEffect}.
 */
public record RegisterDelayedTargetPlayerSpellCastTriggerEffect(
        CardPredicate spellFilter,
        List<CardEffect> resolvedEffects,
        boolean oneShot,
        boolean sourceMustRemainOnBattlefield,
        boolean persistsUntilConsumed
) implements CombatDamageTriggerContextEffect {

    public RegisterDelayedTargetPlayerSpellCastTriggerEffect(
            CardPredicate spellFilter,
            List<CardEffect> resolvedEffects,
            boolean oneShot,
            boolean sourceMustRemainOnBattlefield) {
        this(spellFilter, resolvedEffects, oneShot, sourceMustRemainOnBattlefield, false);
    }

    /** Registers a source-independent one-shot boon that remains until the next matching spell. */
    public static RegisterDelayedTargetPlayerSpellCastTriggerEffect oneShotUntilConsumed(
            CardPredicate spellFilter, List<CardEffect> resolvedEffects) {
        return new RegisterDelayedTargetPlayerSpellCastTriggerEffect(
                spellFilter, resolvedEffects, true, false, true);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
