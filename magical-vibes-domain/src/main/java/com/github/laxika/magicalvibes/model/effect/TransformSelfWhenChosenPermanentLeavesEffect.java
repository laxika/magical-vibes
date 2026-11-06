package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source when the permanent chosen by that source leaves the battlefield, in either
 * direction depending on the source's current face. The permanent that left is the stack entry's triggering
 * permanent.
 */
public record TransformSelfWhenChosenPermanentLeavesEffect() implements CardEffect {
}
