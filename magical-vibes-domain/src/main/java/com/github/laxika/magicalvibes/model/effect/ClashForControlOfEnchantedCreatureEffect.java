package com.github.laxika.magicalvibes.model.effect;

/**
 * Captivating Glance: "At the beginning of your end step, clash with an opponent. If you win,
 * gain control of enchanted creature. Otherwise, that player gains control of enchanted creature."
 * <p>
 * Non-targeting end-step trigger placed on {@code CONTROLLER_END_STEP_TRIGGERED}. The handler
 * performs a clash for the aura's controller and, based on the outcome, moves control of the
 * enchanted creature to the winner of the clash.
 */
public record ClashForControlOfEnchantedCreatureEffect() implements CardEffect {
}
