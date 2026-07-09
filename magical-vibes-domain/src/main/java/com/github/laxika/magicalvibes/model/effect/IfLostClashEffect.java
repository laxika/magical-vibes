package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a clash-trigger effect that is only applied when the controller did <em>not</em> win the
 * clash — the mirror image of {@link IfWonClashEffect}. Consumed by
 * {@code TriggerCollectionService.fireClashTriggers} when firing
 * {@code EffectSlot.ON_CONTROLLER_CLASHES} triggers: the wrapped effect is included in the resolved
 * effect list only when the controller lost (or tied), and dropped on a win.
 *
 * <p>Useful when a clash trigger produces the same base effect regardless of outcome but only a
 * won/lost variant differs (e.g. Rebellion of the Flamekin creates a token either way, but the token
 * gains haste only on a win — modelled as an {@link IfWonClashEffect} won-variant and an
 * {@code IfLostClashEffect} lost-variant so exactly one branch fires).
 *
 * <p>Targeting delegates to the wrapped effect so it shares the trigger's chosen target.
 *
 * @param wrapped the effect applied only on a lost (or tied) clash
 */
public record IfLostClashEffect(CardEffect wrapped) implements CardEffect {

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
