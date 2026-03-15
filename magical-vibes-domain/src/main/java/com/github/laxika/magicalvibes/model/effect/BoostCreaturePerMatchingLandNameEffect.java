package com.github.laxika.magicalvibes.model.effect;

public record BoostCreaturePerMatchingLandNameEffect(
        int powerPerMatch,
        int toughnessPerMatch,
        GrantScope scope
) implements CardEffect {
}
