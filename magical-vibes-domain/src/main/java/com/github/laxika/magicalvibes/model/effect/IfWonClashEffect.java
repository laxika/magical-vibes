package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a clash-trigger effect that is only applied when the controller won the clash
 * (MTG rule 701.29c: "If you won, ..."). Consumed by {@code TriggerCollectionService.performClash}
 * when firing {@code EffectSlot.ON_CONTROLLER_CLASHES} triggers — the wrapped effect is included in
 * the resolved effect list only if the controller won, and dropped otherwise. Since the clash winner
 * is fixed the moment the ability triggers (the clash has already ended), this "if you won" clause is
 * resolved at trigger-creation time rather than by re-checking a condition at resolution.
 *
 * <p>Targeting delegates to the wrapped effect so it shares the trigger's chosen target.
 *
 * @param wrapped the effect applied only on a won clash (e.g. a {@code SkipNextUntapEffect})
 */
public record IfWonClashEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean isSelfTargeting() {
        return wrapped.isSelfTargeting();
    }
}
