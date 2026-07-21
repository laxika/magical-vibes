package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * "Return target permanent to its owner's hand, then [thenEffect]." Bounce sibling of
 * {@link ExileTargetPermanentThenEffect}: the targeted permanent is returned to hand and an
 * existing {@code thenEffect} is resolved afterwards, reusing that effect's own handler.
 *
 * <p>The then-effect is routed by {@link #recipient()}: {@code CONTROLLER} keeps the spell/ability
 * controller; {@code TARGET_CONTROLLER} / {@code TARGET_OWNER} resolve against the bounced
 * permanent's controller / owner (snapshotted before the bounce); {@code TARGET_CONTROLLER_AS_TARGET}
 * / {@code TARGET_OWNER_AS_TARGET} keep the caster as controller but retarget the rider at that
 * player.
 *
 * <p>When {@link #thenCondition()} is non-null it is evaluated <em>after</em> the bounce against the
 * original spell/ability controller (Compelling Deterrence: "then that player discards a card if
 * you control a Zombie" — the Zombie check must see the post-bounce board).
 *
 * @param thenEffect    an existing effect resolved after the bounce
 * @param recipient     whose controller / target slot the then-effect acts on
 * @param thenCondition when non-null, the then-effect happens only if this condition is met after
 *                      the bounce (checked against the original stack entry's controller)
 */
public record ReturnTargetPermanentToHandThenEffect(
        CardEffect thenEffect,
        ThenEffectRecipient recipient,
        Condition thenCondition
) implements CardEffect {

    /** Unconditional then-effect. */
    public ReturnTargetPermanentToHandThenEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(thenEffect, recipient, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
