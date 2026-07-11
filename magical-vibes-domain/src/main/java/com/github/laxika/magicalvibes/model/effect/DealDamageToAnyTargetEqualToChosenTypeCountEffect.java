package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. This deals damage to any target equal to the number of
 * permanents you control of the chosen type." (Roar of the Crowd)
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs, counts the controller's
 * permanents of that type (Changeling-aware), and deals that much damage to the target
 * (creature, planeswalker, or player). Any-target sibling of
 * {@link DealDamageToTargetCreatureEqualToChosenTypeCountEffect}.</p>
 */
public record DealDamageToAnyTargetEqualToChosenTypeCountEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
