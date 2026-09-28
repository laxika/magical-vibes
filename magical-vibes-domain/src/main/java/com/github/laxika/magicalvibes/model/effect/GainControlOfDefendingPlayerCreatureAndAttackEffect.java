package com.github.laxika.magicalvibes.model.effect;

/**
 * "You gain control of a creature the defending player controls until end of turn. If you gain
 * control of a creature this way, tap it, and it's attacking that player."
 *
 * <p>The creature is chosen during resolution, so this effect is deliberately non-targeting.</p>
 */
public record GainControlOfDefendingPlayerCreatureAndAttackEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }
}
