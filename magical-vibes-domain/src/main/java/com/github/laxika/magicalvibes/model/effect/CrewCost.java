package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost for the Crew keyword ability on Vehicle artifacts.
 * "Crew N" means "Tap any number of untapped creatures you control with total power N or greater."
 * The {@link #requiredPower()} is the minimum total power that must be tapped.
 */
public record CrewCost(int requiredPower) implements CostEffect {
}
