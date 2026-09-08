package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * The number of pairs of {@code color} mana spent to cast the spell this amount belongs to.
 * The cast path snapshots the per-color payment totals, so an odd amount contributes only its
 * complete pairs.
 */
public record ColorManaPairsSpentToCast(ManaColor color) implements DynamicAmount {
}
