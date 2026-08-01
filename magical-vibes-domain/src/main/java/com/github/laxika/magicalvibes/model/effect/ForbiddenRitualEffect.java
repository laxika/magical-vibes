package com.github.laxika.magicalvibes.model.effect;

/**
 * Forbidden Ritual: "Sacrifice a nontoken permanent. If you do, target opponent loses
 * {@code lifeLoss} life unless that player sacrifices a permanent of their choice or discards a
 * card. You may repeat this process any number of times."
 *
 * <p>The opponent is the stack entry's {@code targetId} (chosen on cast; repeats keep the same
 * target). The first sacrifice is mandatory if able; each subsequent cycle is offered via
 * {@code PendingInteraction.ForbiddenRitualRepeatChoice}. The opponent's three-way penalty reuses
 * Torment-style plumbing but allows sacrificing <em>any</em> permanent (lands included). Resolved
 * by {@code ForbiddenRitualEffectHandler}.
 *
 * @param lifeLoss life the targeted opponent loses when they neither sacrifice nor discard (2 for
 *                 the printed card)
 */
public record ForbiddenRitualEffect(int lifeLoss) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
