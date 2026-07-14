package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent requires the
 * controller to choose one of Primal Clay's three shapes as it enters the battlefield
 * ("As this creature enters, it becomes your choice of a 3/3 ..., a 2/2 ... with flying, or a 1/6
 * Wall ... with defender"). The chosen shape's base P/T, keyword, and creature type are stamped
 * onto the source permanent when the choice resolves.
 */
public record ChoosePrimalClayFormOnEnterEffect() implements CardEffect {
}
