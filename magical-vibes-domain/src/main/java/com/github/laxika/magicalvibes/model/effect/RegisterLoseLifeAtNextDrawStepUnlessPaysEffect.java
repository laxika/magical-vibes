package com.github.laxika.magicalvibes.model.effect;

/**
 * When it fires as an {@code ON_DAMAGE_TO_PLAYER} triggered ability, registers a delayed trigger
 * against the damaged player: at the beginning of that player's next draw step they lose
 * {@code lifeLoss} life unless they pay {@code {payAmount}} before that draw step. Used by Nafs Asp
 * (1 life / {1}).
 *
 * <p>The damaged player is baked into the firing stack entry's {@code targetId} via
 * {@link CombatDamageTriggerContextEffect.TriggerContext#DAMAGED_PLAYER}; the handler reads it and
 * queues a {@code LoseLifeAtNextDrawStepUnlessPays} delayed action, drained at that player's draw
 * step in {@code StepTriggerService} as a "you may pay; if you don't, lose life" prompt. Modelling
 * the payment at the draw step (rather than any time before it) is outcome-equivalent: paying avoids
 * the loss, declining incurs it.
 *
 * @param lifeLoss  life the damaged player loses if they don't pay
 * @param payAmount generic mana the damaged player may pay to avoid the loss
 */
public record RegisterLoseLifeAtNextDrawStepUnlessPaysEffect(int lifeLoss, int payAmount)
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
