package com.github.laxika.magicalvibes.model.effect;

/**
 * When it fires as an {@code Triggers.dealsDamageToPlayer()} triggered ability, registers a delayed trigger
 * against the damaged player: at the beginning of that player's next upkeep they get {@code amount}
 * poison counters unless they pay {@code manaCost} before that step. Used by Sabertooth Cobra
 * (1 counter / {2}).
 *
 * <p>The damaged player is the firing stack entry's non-targeting {@code targetId}, which the
 * damage-to-a-player collector records; the handler reads it and
 * queues a {@code PoisonAtNextUpkeepUnlessPays} delayed action, drained at that player's upkeep in
 * {@code StepTriggerService} as a "you may pay; if you don't, get a poison counter" prompt.
 * Modelling the payment at the upkeep (rather than any time before it) is outcome-equivalent:
 * paying avoids the counter, declining incurs it.
 *
 * @param amount   poison counters the damaged player gets if they don't pay
 * @param manaCost mana cost the damaged player may pay to avoid the counters
 */
public record RegisterPoisonAtNextUpkeepUnlessPaysEffect(int amount, String manaCost)
        implements CardEffect {
}
