package com.github.laxika.magicalvibes.service.battlefield.etb;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Resolves a single mandatory enter-the-battlefield effect into the form that should be queued at
 * trigger time. Registered per concrete {@link CardEffect} class in {@link EtbEffectResolver}.
 *
 * <p>Handlers cover three shapes of behaviour, all expressed through the return value:
 * <ul>
 *   <li><b>Unwrap / materialise</b> — return a different effect (e.g. a modal {@code ChooseOneEffect}
 *       unwrapped to the chosen option, or {@code GainLifeEqualToToughnessEffect} materialised into a
 *       concrete {@code GainLifeEffect}).</li>
 *   <li><b>Intervening-if gate</b> (CR 603.4) — return the effect unchanged when the condition is met,
 *       or {@code null} to drop the trigger entirely when it is not.</li>
 *   <li><b>Conditional no-op</b> — return {@code null} to drop the trigger (e.g. a cast-from-hand
 *       effect when the permanent was not cast from hand).</li>
 * </ul>
 */
@FunctionalInterface
public interface EtbEffectHandler {

    /**
     * @param ctx    the trigger-time context (entering card, controller, cast flags, modal index)
     * @param effect the raw effect from the {@code ON_ENTER_BATTLEFIELD} slot
     * @return the resolved effect to queue, or {@code null} to drop the trigger
     */
    CardEffect resolve(EtbEffectContext ctx, CardEffect effect);
}
