package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this permanent unless you sacrifice any number of creatures with total power
 * {@code requiredPower} or greater" (Phyrexian Dreadnought).
 *
 * <p>The controller picks any number of the other creatures they control; the selection is only
 * legal when its total effective power reaches {@code requiredPower}. An empty selection (or not
 * controlling enough power to reach the threshold at all) sacrifices the source instead.
 */
public record SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect(int requiredPower) implements CardEffect {
}
