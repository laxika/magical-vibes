package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a nonland card, reveals the chosen card, and shuffles it
 * back into the library. The selected card name is carried to the following life-loss effect by
 * the library-search completion flow.
 */
public record SearchLibraryForNonlandCardAndRevealEffect(int lifeLoss) implements CardEffect {
}
