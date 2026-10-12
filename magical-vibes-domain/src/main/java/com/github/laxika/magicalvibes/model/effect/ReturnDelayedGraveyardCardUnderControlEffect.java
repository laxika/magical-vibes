package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;

/**
 * Resolution of a delayed "put that card onto the battlefield under [a player's] control at the
 * beginning of the next end step" trigger. It carries the scheduled return so the trigger can go on
 * the stack when the end step begins and do its work when it resolves.
 *
 * @param pending the scheduled return this trigger performs
 */
public record ReturnDelayedGraveyardCardUnderControlEffect(
        DelayedGraveyardToBattlefieldUnderControl pending) implements CardEffect {
}
