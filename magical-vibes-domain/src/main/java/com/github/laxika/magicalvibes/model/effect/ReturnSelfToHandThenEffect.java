package com.github.laxika.magicalvibes.model.effect;

/** Returns the source permanent to its owner's hand, then resolves the payload if it returned. */
public record ReturnSelfToHandThenEffect(CardEffect thenEffect)
        implements CombatDamageTriggerContextEffect {

    public ReturnSelfToHandThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ReturnSelfToHandThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec payload = thenEffect.targetSpec();
        return new TargetSpec(payload.declaredTarget(), payload.harmful(), payload.predicate(), true,
                payload.playerTargetCount());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
