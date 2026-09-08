package com.github.laxika.magicalvibes.model.effect;

/**
 * The triggered ability that follows a player gaining control of Risky Move from another player.
 */
public record RiskyMoveEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
