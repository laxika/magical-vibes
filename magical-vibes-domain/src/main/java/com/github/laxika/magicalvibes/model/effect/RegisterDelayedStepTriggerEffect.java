package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Objects;

/**
 * "At the beginning of [step], [effects]" made after an earlier step of the same resolution and about what that step
 * did ("exile those tokens", "return those cards to their owners' hands"): a delayed triggered ability that triggers
 * the next time its step begins (CR 603.7b). Its effects name those objects as a later step of the resolution would:
 * {@code Subjects.cardsThisWay(EXILE)}, {@code AmountThisWay.stillExiled()}, {@code Subjects.createdThisResolution()}.
 * "Sacrifice it at the beginning of the next end step" after "put it onto the battlefield" is
 * {@link #aboutPutOntoBattlefield}: "it" is the permanent the card became, the ability's triggering permanent when it
 * is one. An object that left its zone since is a new object and isn't affected (CR 603.7c, 400.7). An ability that does
 * nothing but act on those objects is not created when there are none, as it could affect nothing; one that does more
 * ("return those cards to your hand, then draw a card") always is ({@code alsoIfNothingHappened}, CR 603.7a). One about
 * no earlier step ("discard three cards at the beginning of the next end step") is {@link #atNext}.
 *
 * @param about            the step it is about: the most recent one of that kind, or, for
 *                         {@link StepKind#CREATE_TOKEN}, every permanent this resolution created; {@code null} for
 *                         none, an ability that is always created and names no object it is about
 * @param step             the step it waits for
 * @param effects          what it does
 * @param player           the player it is about ("that player's next turn", {@code ActivePlayer.CHOSEN_PLAYER}), its
 *                         entry's non-targeting player; {@code null} for none
 * @param afterThisTurn    whether it waits past this turn ("of that player's next turn")
 * @param ifAnyStillExiled whether it has the intervening "if any of those cards remain exiled", "if that player hasn't
 *                         played the card" (CR 603.4)
 * @param alsoIfNothingHappened whether it does more than act on those objects, so it is created even when that step
 *                         did nothing
 * @param permanent        the permanent it is about when it is about no step ({@link #aboutSourceAtNext}): its entry's
 *                         non-targeting object and triggering permanent, which it doesn't affect once that permanent
 *                         has left the battlefield (CR 603.7c); not created when the subject names none; {@code null}
 *                         for none
 * @param orStep           another step it waits for, whichever begins first ("the next main phase": the precombat or
 *                         the postcombat main phase); {@code null} for none
 * @param thisTurn         whether it has the stated duration "this turn" ("at the beginning of your next main phase
 *                         this turn"): it ends with the turn if it hasn't triggered (CR 603.7b)
 */
public record RegisterDelayedStepTriggerEffect(StepKind about, StepTrigger step, List<CardEffect> effects,
                                               PlayerSubject player, boolean afterThisTurn,
                                               boolean ifAnyStillExiled, boolean alsoIfNothingHappened,
                                               PermanentSubject permanent, StepTrigger orStep, boolean thisTurn)
        implements CardEffect {

    public RegisterDelayedStepTriggerEffect {
        Objects.requireNonNull(step, "step");
        effects = List.copyOf(effects);
        if (about == null && (ifAnyStillExiled || !alsoIfNothingHappened)) {
            throw new IllegalArgumentException("An ability about no step is always created and has no cards to check");
        }
        if (about != null && about != StepKind.EXILE && about != StepKind.CREATE_TOKEN && about != StepKind.SEEK
                && about != StepKind.RETURN) {
            throw new IllegalArgumentException("A delayed ability is about exiled or sought cards or permanents "
                    + "created or put onto the battlefield: " + about);
        }
        if (player != null && player.declaresTarget()) {
            throw new IllegalArgumentException("The step that did it declares the target: " + player);
        }
        if (permanent != null && (about != null || permanent.declaresTarget())) {
            throw new IllegalArgumentException("An ability about a permanent is about no step, and is made about it, "
                    + "not targeting it: " + permanent);
        }
        if ((thisTurn || orStep != null) && (about != null || permanent != null || player != null || afterThisTurn)) {
            throw new IllegalArgumentException(
                    "Only an ability about nothing waits for either of two steps or this turn");
        }
        if (ifAnyStillExiled && alsoIfNothingHappened) {
            throw new IllegalArgumentException("With no cards exiled none remains exiled, so it could never trigger");
        }
        if (ifAnyStillExiled && about != StepKind.EXILE && about != StepKind.SEEK) {
            throw new IllegalArgumentException("Only exiled or sought cards can still be where they went: " + about);
        }
    }

    /** At the next {@code step}, {@code effects}, about no earlier step ("at the beginning of the next end step, …"). */
    public static RegisterDelayedStepTriggerEffect atNext(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(null, step, List.of(effects), null, false, false, true, null, null,
                false);
    }

    /**
     * At the beginning of {@code player}'s next main phase this turn ({@code ActivePlayer.YOU}: "your next main phase
     * this turn"; {@code ActivePlayer.ANY}: "the next main phase this turn"), precombat or postcombat, {@code effects},
     * about no earlier step. It ends with the turn if no such main phase begins (CR 603.7b).
     */
    public static RegisterDelayedStepTriggerEffect atNextMainPhaseThisTurn(ActivePlayer player, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(null, Triggers.precombatMainPhase(player), List.of(effects), null,
                false, false, true, null, Triggers.postcombatMainPhase(player), true);
    }

    /**
     * At the next {@code step}, {@code effects} about this permanent ("transform this creature at the beginning of the
     * next upkeep"), the source of the spell or ability: not created when it has none.
     */
    public static RegisterDelayedStepTriggerEffect aboutSourceAtNext(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(null, step, List.of(effects), null, false, false, true,
                Subjects.source(), null, false);
    }

    /** At {@code step}, {@code effects} about the cards the last exile step exiled. */
    public static RegisterDelayedStepTriggerEffect aboutExiled(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(StepKind.EXILE, step, List.of(effects), null, false, false, false,
                null, null, false);
    }

    /** At {@code step}, {@code effects} about the permanents created this way. */
    public static RegisterDelayedStepTriggerEffect aboutCreated(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(StepKind.CREATE_TOKEN, step, List.of(effects), null, false, false,
                false, null, null, false);
    }

    /** At {@code step}, {@code effects} about the cards the last seek put into a hand. */
    public static RegisterDelayedStepTriggerEffect aboutSought(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(StepKind.SEEK, step, List.of(effects), null, false, false, false,
                null, null, false);
    }

    /** At {@code step}, {@code effects} about the permanents the last put or return onto the battlefield made. */
    public static RegisterDelayedStepTriggerEffect aboutPutOntoBattlefield(StepTrigger step, CardEffect... effects) {
        return new RegisterDelayedStepTriggerEffect(StepKind.RETURN, step, List.of(effects), null, false, false,
                false, null, null, false);
    }

    /** This ability, waiting for a turn after this one ("at the beginning of your next turn's end step"). */
    public RegisterDelayedStepTriggerEffect waitingForALaterTurn() {
        return new RegisterDelayedStepTriggerEffect(about, step, effects, player, true, ifAnyStillExiled,
                alsoIfNothingHappened, permanent, orStep, thisTurn);
    }

    /** This ability, about {@code thatPlayer} as well, waiting for a turn after this one. */
    public RegisterDelayedStepTriggerEffect ofThatPlayersNextTurn(PlayerSubject thatPlayer) {
        return new RegisterDelayedStepTriggerEffect(about, step, effects, Objects.requireNonNull(thatPlayer, "player"),
                true, ifAnyStillExiled, alsoIfNothingHappened, permanent, orStep, thisTurn);
    }

    /** This ability with "if any of those cards remain exiled". */
    public RegisterDelayedStepTriggerEffect onlyIfAnyStillExiled() {
        return new RegisterDelayedStepTriggerEffect(about, step, effects, player, afterThisTurn, true,
                alsoIfNothingHappened, permanent, orStep, thisTurn);
    }

    /** This ability with "if that card is in your hand", of one about sought cards. */
    public RegisterDelayedStepTriggerEffect onlyIfStillInHand() {
        if (about != StepKind.SEEK) {
            throw new IllegalArgumentException("Only sought cards went to a hand: " + about);
        }
        return new RegisterDelayedStepTriggerEffect(about, step, effects, player, afterThisTurn, true,
                alsoIfNothingHappened, permanent, orStep, thisTurn);
    }

    /** This ability, which does more than act on those objects ("..., then draw a card"). */
    public RegisterDelayedStepTriggerEffect evenIfNothingHappened() {
        return new RegisterDelayedStepTriggerEffect(about, step, effects, player, afterThisTurn, ifAnyStillExiled,
                true, permanent, orStep, thisTurn);
    }
}
