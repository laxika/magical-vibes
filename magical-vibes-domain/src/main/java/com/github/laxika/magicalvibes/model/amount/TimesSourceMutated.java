package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of times the source permanent has mutated. The count belongs to the permanent and
 * survives turn cleanup while resetting when the permanent changes zones.
 */
public record TimesSourceMutated() implements DynamicAmount {
}
