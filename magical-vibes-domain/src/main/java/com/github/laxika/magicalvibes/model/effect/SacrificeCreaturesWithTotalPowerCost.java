package com.github.laxika.magicalvibes.model.effect;

/**
 * Ransom: "sacrifice any number of creatures with total power {@code totalPower} or greater" (Phyrexian Dreadnought).
 * The payer chooses any of the creatures they control that can be sacrificed, whose total power is at least
 * {@code totalPower}, and sacrifices them together. Paid only as an effect resolves ({@code UnlessPaysEffect}).
 *
 * @param totalPower the least total power the chosen creatures must have
 */
public record SacrificeCreaturesWithTotalPowerCost(int totalPower) implements CostEffect {

    public SacrificeCreaturesWithTotalPowerCost {
        if (totalPower < 1) {
            throw new IllegalArgumentException("A total power to reach is at least 1: " + totalPower);
        }
    }
}
