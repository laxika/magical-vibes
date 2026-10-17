package com.github.laxika.magicalvibes.model.effect;

/**
 * Switches the power and toughness of every creature on the battlefield. The affected creatures are
 * selected when the effect resolves. Lasts until end of turn unless {@code indefinite} is true, in which
 * case the switch has no duration and lasts for the rest of the game.
 */
public record SwitchAllCreaturesPowerToughnessEffect(boolean indefinite) implements CardEffect {

    /** Until-end-of-turn switch. */
    public SwitchAllCreaturesPowerToughnessEffect() {
        this(false);
    }
}
