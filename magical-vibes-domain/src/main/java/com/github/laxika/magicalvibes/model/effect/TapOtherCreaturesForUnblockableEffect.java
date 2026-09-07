package com.github.laxika.magicalvibes.model.effect;

/**
 * Attack trigger: the controller may tap exactly the configured number of other untapped
 * creatures they control. If they do, the source creature can't be blocked this turn.
 */
public record TapOtherCreaturesForUnblockableEffect(int creatureCount) implements CardEffect {

    public TapOtherCreaturesForUnblockableEffect {
        if (creatureCount < 1) {
            throw new IllegalArgumentException("creatureCount must be positive");
        }
    }
}
