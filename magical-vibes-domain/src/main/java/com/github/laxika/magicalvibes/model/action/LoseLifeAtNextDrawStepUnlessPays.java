package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Delayed trigger: at the beginning of {@code playerId}'s next draw step they lose {@code lifeLoss}
 * life unless they pay {@code {payAmount}} before that draw step while they have priority.
 * Paying is a special action (CR 116.2c, {@code AbilityActivationService.payDelayedTriggerCost},
 * offered through {@code GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy}) that removes
 * one obligation immediately without using the stack.
 * Scheduled by Nafs Asp's {@code ON_DAMAGE_TO_PLAYER} trigger and drained in
 * {@code StepTriggerService} when that player's draw step begins. Persists across intervening turns
 * until the affected player's own draw step is reached. {@code controllerId} is the player who
 * controlled the ability that created this delayed trigger (CR 603.7e), not necessarily {@code playerId}.
 */
public record LoseLifeAtNextDrawStepUnlessPays(UUID playerId, int lifeLoss, int payAmount, Card sourceCard,
                                               UUID controllerId)
        implements DelayedAction {

    public LoseLifeAtNextDrawStepUnlessPays(UUID playerId, int lifeLoss, int payAmount, Card sourceCard) {
        this(playerId, lifeLoss, payAmount, sourceCard, playerId);
    }
}
