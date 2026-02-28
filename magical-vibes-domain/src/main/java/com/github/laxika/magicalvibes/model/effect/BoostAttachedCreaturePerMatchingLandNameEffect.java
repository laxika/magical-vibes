package com.github.laxika.magicalvibes.model.effect;

public record BoostAttachedCreaturePerMatchingLandNameEffect(int powerPerMatch,
                                                              int toughnessPerMatch) implements CardEffect {
}
