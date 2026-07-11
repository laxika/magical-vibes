package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. Target creature gets {@code powerPer}/{@code toughnessPer} until end of
 * turn for each permanent of the chosen type you control." (Pack's Disdain)
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs, counts the controller's permanents
 * of that type (Changeling-aware), and applies a {@code (count * powerPer)/(count * toughnessPer)}
 * modifier to the target creature until end of turn. Boost sibling of
 * {@link DealDamageToTargetCreatureEqualToChosenTypeCountEffect} and
 * {@link DrawCardPerChosenTypeCountEffect}.</p>
 */
public record BoostTargetCreaturePerChosenTypeCountEffect(int powerPer, int toughnessPer) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
