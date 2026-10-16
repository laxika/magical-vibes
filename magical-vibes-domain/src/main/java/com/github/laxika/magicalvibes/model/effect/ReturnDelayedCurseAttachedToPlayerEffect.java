package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.DelayedReturnCurseAttachedToPlayer;

/**
 * Resolution of a delayed "return that Curse to the battlefield attached to [a player] at the beginning of
 * the next end step" trigger. It carries the scheduled return so the trigger can go on the stack when the
 * end step begins and do its work when it resolves.
 *
 * @param pending the scheduled return this trigger performs
 */
public record ReturnDelayedCurseAttachedToPlayerEffect(
        DelayedReturnCurseAttachedToPlayer pending) implements CardEffect {
}
