package com.github.laxika.magicalvibes.model.amount;

/**
 * Evaluates to {@code amount} when the targeted player controls strictly more lands than the
 * controller, and to {@code otherwise} otherwise (missing target → {@code otherwise}).
 *
 * <p>Models Tithe's "search for a Plains; if target opponent controls more lands than you, you
 * may search for an additional Plains" as a single up-to-{@code amount}/up-to-{@code otherwise}
 * library search counted at resolution.
 */
public record FixedIfTargetPlayerControlsMoreLands(int amount, int otherwise) implements DynamicAmount {
}
