package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static companion for {@link TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect}. It applies
 * the effect's fixed basic land type to every land recorded by the source permanent.
 */
public record TrackedLandsBecomeBasicLandTypeEffect(CardSubtype subtype) implements CardEffect {
}
