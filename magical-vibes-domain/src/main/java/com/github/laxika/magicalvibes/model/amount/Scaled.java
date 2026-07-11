package com.github.laxika.magicalvibes.model.amount;

/** A base amount multiplied by a constant factor (e.g. "+2/+0 for each Equipment" = Scaled(count, 2)). */
public record Scaled(DynamicAmount amount, int factor) implements DynamicAmount {
}
