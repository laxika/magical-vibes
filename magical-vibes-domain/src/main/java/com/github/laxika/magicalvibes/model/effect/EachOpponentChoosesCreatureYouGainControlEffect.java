package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each opponent chooses a creature they control. You gain control of those creatures."
 *
 * <p>Non-targeting. Opponents choose in APNAP/turn order (0 creatures ⇒ skip, 1 ⇒ auto,
 * 2+ ⇒ prompt). After every opponent has chosen, the controller gains permanent control of all
 * chosen creatures simultaneously. Used by Riches (Rags // Riches aftermath half).
 */
public record EachOpponentChoosesCreatureYouGainControlEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
