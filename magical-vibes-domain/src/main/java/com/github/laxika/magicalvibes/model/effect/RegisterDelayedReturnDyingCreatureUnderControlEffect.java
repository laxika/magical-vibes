package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;


/**
 * Death-trigger effect for "put that card onto the battlefield under your control at the beginning
 * of the next end step": Seraph ({@code Triggers.creatureDies(DAMAGED_BY_THIS)}), Grave Betrayal
 * ({@code Triggers.creatureDies(OPPONENT_CONTROLS)}) and Shirei, Shizo's Caretaker
 * ({@code Triggers.creatureDies(YOU_CONTROL)}, wrapped in a {@code MayEffect}), plus Lifeline's owner-control
 * return from {@code Triggers.creatureDies(ANY)}.
 *
 * <p>On resolution the handler reads the dying creature's card from the stack entry's triggering card
 * ({@code StackEntry.triggeringCardId}) and its graveyard incarnation, then queues a
 * {@code DelayedGraveyardToBattlefieldUnderControl} carrying the riders below, drained in
 * {@code StepTriggerService.handleEndStepTriggers}.
 *
 * @param sacrificeOnSourceControlLoss when true the returned permanent is linked to the source
 *                                     permanent so it is sacrificed if its controller later loses
 *                                     control of the source (Seraph's "Sacrifice the creature when
 *                                     you lose control of this creature")
 * @param counterType                  when non-null, the returned permanent enters with
 *                                     {@code counterAmount} counters of this type
 * @param counterAmount                number of {@code counterType} counters to enter with
 * @param grantColor                   when non-null, permanently granted to the returned permanent
 *                                     "in addition to its other colors"
 * @param grantSubtype                 when non-null, permanently granted to the returned permanent
 *                                     "in addition to its other types"
 * @param requireSourceOnBattlefield   when true the delayed return does nothing unless the exact
 *                                     source permanent is still on the battlefield at the end step
 *                                     (Shirei's "if Shirei is still on the battlefield" — a Shirei
 *                                     that left and came back is a new object and does not count)
 * @param returnUnderOwnersControl      when true, return the card under its owner's control instead
 *                                     of the ability controller's control (Lifeline)
 * @param targetOpponent               when true, the death trigger targets an opponent and that
 *                                     player becomes the controller of the delayed return
 * @param optionalReturn               when true the delayed end-step trigger asks its controller
 *                                     whether to return the creature as it resolves (Shirei's
 *                                     "you may return that card ... at the beginning of the next
 *                                     end step")
 */
public record RegisterDelayedReturnDyingCreatureUnderControlEffect(
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype,
        boolean requireSourceOnBattlefield,
        boolean returnUnderOwnersControl,
        boolean targetOpponent,
        boolean optionalReturn
) implements CardEffect {

    /** Seraph: plain return linked to the source permanent, no counters and no grants. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect() {
        this(true, null, 0, null, null, false, false, false, false);
    }

    /** Grave Betrayal-style: counter and colour/subtype riders, no source-presence requirement. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect(boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype) {
        this(sacrificeOnSourceControlLoss, counterType, counterAmount, grantColor, grantSubtype,
                false, false, false, false);
    }

    /**
     * Lifeline-style: return under the card's owner's control (Lifeline's "if another creature is on the battlefield"
     * is its ability's intervening "if").
     */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect(boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype,
            boolean requireSourceOnBattlefield, boolean returnUnderOwnersControl) {
        this(sacrificeOnSourceControlLoss, counterType, counterAmount, grantColor, grantSubtype,
                requireSourceOnBattlefield, returnUnderOwnersControl, false, false);
    }

    /**
     * Shirei: no riders, the controller may decline as the delayed trigger resolves, and the return
     * only happens while that same Shirei is still around.
     */
    public static RegisterDelayedReturnDyingCreatureUnderControlEffect optionalWhileSourceRemains() {
        return new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                false, null, 0, null, null, true, false, false, true);
    }

    /** Endless Whispers: the creature's controller chooses an opponent to control the return. */
    public static RegisterDelayedReturnDyingCreatureUnderControlEffect forOpponent() {
        return new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                false, null, 0, null, null, false, false, true, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetOpponent ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return targetOpponent ? PlayerRelation.OPPONENT : PlayerRelation.ANY;
    }
}
