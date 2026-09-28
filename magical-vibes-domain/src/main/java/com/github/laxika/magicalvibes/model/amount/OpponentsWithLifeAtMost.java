package com.github.laxika.magicalvibes.model.amount;

/** The number of opponents whose life total is at or below the threshold. */
public record OpponentsWithLifeAtMost(int threshold) implements DynamicAmount {
}
