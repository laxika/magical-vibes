package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Gives control of the source permanent to the player carried on the stack entry. The normal form
 * targets that player; {@link #triggeringPlayer()} uses a non-targeting player supplied by the
 * surrounding trigger. The overload with a follow-up effect is used for combat-damage abilities
 * whose follow-up happens only after control changes successfully.
 */
public record TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer, CardEffect thenEffect)
        implements CardEffect, CombatDamageTriggerContextEffect, CombatDamageAmountAwareEffect {

    public TargetPlayerGainsControlOfSourceCreatureEffect() {
        this(true, null);
    }

    public TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer) {
        this(targetsPlayer, null);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer() {
        return new TargetPlayerGainsControlOfSourceCreatureEffect(false, null);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer(CardEffect thenEffect) {
        return new TargetPlayerGainsControlOfSourceCreatureEffect(false, thenEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return thenEffect == null ? null : TriggerContext.DAMAGED_PLAYER;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return thenEffect == null
                ? new Fixed(0)
                : new EventValue();
    }
}
