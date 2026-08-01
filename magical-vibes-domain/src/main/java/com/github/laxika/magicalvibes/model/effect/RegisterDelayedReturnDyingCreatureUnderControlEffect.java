package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death-trigger effect for "put that card onto the battlefield under your control at the beginning
 * of the next end step": Seraph ({@code ON_DAMAGED_CREATURE_DIES}) and Grave Betrayal
 * ({@code ON_OPPONENT_CREATURE_DIES}).
 *
 * <p>On resolution the handler reads the dying creature's card id from the trigger entry
 * ({@code StackEntry.triggeringCardId}) and queues a {@code DelayedGraveyardToBattlefieldUnderControl}
 * carrying the riders below, drained in {@code StepTriggerService.handleEndStepTriggers}.
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
 */
public record RegisterDelayedReturnDyingCreatureUnderControlEffect(
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype
) implements CardEffect {

    /** Seraph: plain return linked to the source permanent, no counters and no grants. */
    public RegisterDelayedReturnDyingCreatureUnderControlEffect() {
        this(true, null, 0, null, null);
    }
}
