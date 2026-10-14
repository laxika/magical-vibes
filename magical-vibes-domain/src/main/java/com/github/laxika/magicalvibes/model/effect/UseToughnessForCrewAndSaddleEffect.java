package com.github.laxika.magicalvibes.model.effect;

/**
 * Uses the source creature's effective toughness instead of its power for crew and saddle costs.
 *
 * @param crewOnly {@code true} to apply only when the source pays a crew cost (Giant Ox); {@code false}
 *                 to apply to saddle costs as well (Interface Ace)
 */
public record UseToughnessForCrewAndSaddleEffect(boolean crewOnly) implements CrewAndSaddlePowerModifierEffect {

    public UseToughnessForCrewAndSaddleEffect() {
        this(false);
    }

    @Override
    public int powerBonus() {
        return 0;
    }

    @Override
    public boolean usesToughnessInsteadOfPower() {
        return true;
    }
}
