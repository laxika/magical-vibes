package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "Sacrifice this permanent. If you do, {@code thenEffect}." Sacrifices the stack entry's source
 * permanent and resolves {@code thenEffect} only when the sacrifice actually happened — the
 * contingency a bare list of effects (or a {@link SequenceEffect}, which explicitly has none)
 * cannot express. A source that has already left the battlefield can't be sacrificed, so nothing
 * else happens either.
 *
 * <p>Pair with a {@link MayEffect} wrapper for the common "you may sacrifice it. If you do, …"
 * template (Impaler Shrike, Mindstab Thrull): the may/sacrifice choice waits for resolution
 * (CR 603.5), while any target the payload declares is chosen as the trigger goes on the stack
 * (CR 603.3d).
 *
 * @param thenEffect the payload resolved after a successful sacrifice
 */
public record SacrificeSelfThenEffect(CardEffect thenEffect)
        implements CombatDamageTriggerContextEffect, DyingCreatureCardAwareEffect {

    public SacrificeSelfThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("SacrificeSelfThenEffect requires a payload; use SacrificeSelfEffect for a bare sacrifice");
        }
    }

    /**
     * Forwards the binding to the payload — on a death trigger it is the payload that acts on the
     * dying card (Angelic Renewal), never the sacrifice half.
     */
    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        if (thenEffect instanceof DyingCreatureCardAwareEffect aware) {
            return new SacrificeSelfThenEffect(aware.boundToDyingCard(dyingCardId));
        }
        return this;
    }

    /** Targeting is the payload's — the sacrifice half never targets. */
    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }

    /**
     * On an {@code ON_COMBAT_DAMAGE_TO_PLAYER} slot the sacrifice half always needs the source
     * permanent bound, so {@code SOURCE_SELF} is the floor. A payload that also needs the damaged
     * player ({@code DAMAGED_PLAYER}) asks for a superset of that and wins.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        if (thenEffect instanceof CombatDamageTriggerContextEffect contextEffect
                && contextEffect.combatDamageTriggerContext() == TriggerContext.DAMAGED_PLAYER) {
            return TriggerContext.DAMAGED_PLAYER;
        }
        return TriggerContext.SOURCE_SELF;
    }
}
