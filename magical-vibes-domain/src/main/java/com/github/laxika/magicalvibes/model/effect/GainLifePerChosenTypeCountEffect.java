package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. You gain N life for each permanent you control of that type."
 * (Luminescent Rain)
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs, counts the controller's
 * permanents of that type (Changeling-aware), and gains {@code lifePerPermanent} life for each.</p>
 */
public record GainLifePerChosenTypeCountEffect(int lifePerPermanent) implements CardEffect {
}
