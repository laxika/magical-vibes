package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that mills cards from the controller's library as part of an activated ability's cost.
 * Unlike {@link MillControllerEffect}, this is paid during activation (before resolution) and
 * the ability cannot be activated if the controller's library has fewer cards than required.
 */
public record MillControllerCost(int count) implements CostEffect {
}
