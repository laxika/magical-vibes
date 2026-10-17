package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost for the Crew keyword ability on Vehicle artifacts.
 * "Crew N" means "Tap any number of untapped creatures you control with total power N or greater."
 * The {@link #requiredPower()} is the minimum total power that must be tapped.
 *
 * @param requiredPower the minimum total power that must be tapped
 * @param crewsVehicle  {@code true} when the tapped creatures crew a Vehicle, so crew restrictions
 *                      ("can't crew Vehicles") and crew triggers apply; {@code false} for an
 *                      ordinary cost that merely taps creatures for their total power
 */
public record CrewCost(int requiredPower, boolean crewsVehicle) implements PowerBasedTapCost {

    public CrewCost(int requiredPower) {
        this(requiredPower, true);
    }

    /** "Tap any number of untapped creatures you control with total power N or greater" that is not Crew. */
    public static CrewCost tapCreaturesWithTotalPower(int requiredPower) {
        return new CrewCost(requiredPower, false);
    }
}
