package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * When resolved, returns the triggering card (the stack entry's card) from its owner's graveyard to
 * the battlefield (tapped if {@code enterTapped}) under its owner's control, or under the
 * triggered ability controller's control when {@code returnUnderController} is true.
 * Fizzles silently otherwise.
 * <p>
 * Pushed as a triggered ability by the death pipeline for delayed "return that card when it dies this
 * turn" effects (e.g. Graceful Reprieve untapped, Supernatural Stamina tapped). Also used directly on a
 * death trigger, e.g. Angelic Renewal's {@code (false, true)} "if you do" payload after a {@link SacrificeSelfEffect}.
 * The counter amount is evaluated as it resolves: "with one fewer revival counter on it" of a delayed return is the
 * number its maker noted ({@code AmountThisWay.noted()}, Nine-Lives Familiar), fixed as that ability resolved
 * (CR 608.2h).
 */
public record ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped,
                                                                   boolean returnUnderController,
                                                                   CounterType counterType,
                                                                   DynamicAmount counterAmount,
                                                                   CardSubtype grantSubtype)
        implements CardEffect {

    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped, boolean returnUnderController,
                                                                 CounterType counterType, int counterAmount,
                                                                 CardSubtype grantSubtype) {
        this(enterTapped, returnUnderController, counterType, new Fixed(counterAmount), grantSubtype);
    }

    /** Convenience for the untapped return (Graceful Reprieve). */
    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect() {
        this(false, false, null, 0, null);
    }

    /** Convenience for the owner's-control return with configurable tapped entry. */
    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped) {
        this(enterTapped, false, null, 0, null);
    }

    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped,
                                                                 boolean returnUnderController) {
        this(enterTapped, returnUnderController, null, 0, null);
    }

    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped,
                                                                 boolean returnUnderController,
                                                                 CounterType counterType,
                                                                 int counterAmount) {
        this(enterTapped, returnUnderController, counterType, counterAmount, null);
    }

    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped,
                                                                 boolean returnUnderController,
                                                                 CounterType counterType,
                                                                 DynamicAmount counterAmount) {
        this(enterTapped, returnUnderController, counterType, counterAmount, null);
    }
}
