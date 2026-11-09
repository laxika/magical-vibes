package com.github.laxika.magicalvibes.model.effect;

/**
 * When it fires as an {@code Triggers.dealsDamageToPlayer()} triggered ability, registers a delayed trigger
 * against the damaged player: at the beginning of that player's next draw step they lose
 * {@code lifeLoss} life unless they pay {@code {payAmount}} before that draw step. Used by Nafs Asp
 * (1 life / {1}).
 *
 * <p>The damaged player is the firing stack entry's non-targeting {@code targetId}, which the
 * damage-to-a-player collector records; the handler reads it and
 * queues a {@code LoseLifeAtNextDrawStepUnlessPays} delayed action, drained at that player's draw
 * step in {@code StepTriggerService} as a "you may pay; if you don't, lose life" prompt. Modelling
 * the payment at the draw step (rather than any time before it) is outcome-equivalent: paying avoids
 * the loss, declining incurs it.
 *
 * @param lifeLoss  life the damaged player loses if they don't pay
 * @param payAmount generic mana the damaged player may pay to avoid the loss
 */
public record RegisterLoseLifeAtNextDrawStepUnlessPaysEffect(int lifeLoss, int payAmount)
        implements CardEffect {
}
