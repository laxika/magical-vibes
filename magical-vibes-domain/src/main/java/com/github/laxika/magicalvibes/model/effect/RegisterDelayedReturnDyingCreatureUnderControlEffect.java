package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Death-trigger effect for "put that card onto the battlefield under your control at the beginning
 * of the next end step": Seraph ({@code ON_DAMAGED_CREATURE_DIES}), Grave Betrayal
 * ({@code ON_OPPONENT_CREATURE_DIES}) and Shirei, Shizo's Caretaker
 * ({@code ON_ALLY_CREATURE_DIES}, wrapped in a {@code MayEffect}), plus Lifeline's owner-control
 * return from {@code ON_ANY_CREATURE_DIES}.
 *
 * <p>On resolution the handler reads the dying creature's card id from {@code dyingCardId} when the
 * collector bound one (the {@code MayEffect} flow does not carry the stack entry's triggering-card
 * id) and otherwise from the trigger entry ({@code StackEntry.triggeringCardId}), then queues a
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
 * @param requireAnotherCreature        when true, the trigger is only collected and resolved while
 *                                     another creature is on the battlefield (Lifeline)
 * @param dyingCardId                  the dying creature's card id, bound by the trigger collector
 *                                     on the {@code MayEffect} path; null lets the handler fall back
 *                                     to the stack entry's triggering-card id
 */
public record RegisterDelayedReturnDyingCreatureUnderControlEffect(
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype,
        boolean requireSourceOnBattlefield,
        boolean returnUnderOwnersControl,
        boolean requireAnotherCreature,
        UUID dyingCardId
) implements CardEffect, DyingCreatureCardAwareEffect {

    /** Seraph: plain return linked to the source permanent, no counters and no grants. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect() {
        this(true, null, 0, null, null, false, false, false, null);
    }

    /** Grave Betrayal-style: counter and colour/subtype riders, no source-presence requirement. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect(boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype) {
        this(sacrificeOnSourceControlLoss, counterType, counterAmount, grantColor, grantSubtype,
                false, false, false, null);
    }

    /** Lifeline-style: return under the card's owner's control while another creature remains. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect(boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype,
            boolean requireSourceOnBattlefield, boolean returnUnderOwnersControl,
            boolean requireAnotherCreature) {
        this(sacrificeOnSourceControlLoss, counterType, counterAmount, grantColor, grantSubtype,
                requireSourceOnBattlefield, returnUnderOwnersControl, requireAnotherCreature, null);
    }

    /** Shirei: no riders, but the return only happens while that same Shirei is still around. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect(boolean requireSourceOnBattlefield) {
        this(false, null, 0, null, null, requireSourceOnBattlefield, false, false, null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new RegisterDelayedReturnDyingCreatureUnderControlEffect(sacrificeOnSourceControlLoss,
                counterType, counterAmount, grantColor, grantSubtype, requireSourceOnBattlefield,
                returnUnderOwnersControl, requireAnotherCreature, dyingCardId);
    }
}
