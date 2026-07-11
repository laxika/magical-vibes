package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. Draw a card for each permanent you control of that type." (Distant Melody)
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs, counts the controller's
 * permanents of that type (Changeling-aware), and draws that many cards.</p>
 */
public record DrawCardPerChosenTypeCountEffect() implements CardEffect {
}
