package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes one counter of {@code counterType} from the source Aura. If that was the last such counter,
 * the permanent the Aura is attached to is destroyed and the Aura deals {@code damageToController}
 * damage to that permanent's controller.
 *
 * <p>Orcish Mine's "remove an ore counter" and its "when the last ore counter is removed" trigger live in
 * one effect: the removal reliably produces the follow-up in the same resolution, which is what the two
 * printed abilities amount to in a two-player game.
 */
public record RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect(CounterType counterType, int damageToController)
        implements CardEffect {
}
