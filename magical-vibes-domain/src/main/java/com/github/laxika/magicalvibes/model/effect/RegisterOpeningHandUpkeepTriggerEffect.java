package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals an opening-hand card and schedules its mandatory first-upkeep effect.
 *
 * @param effect                the effect put on the stack at that upkeep
 * @param revealersFirstUpkeep  {@code true} for "at the beginning of your first upkeep" (Sphinx of Foresight);
 *                              {@code false} for "at the beginning of the first upkeep" of the game (the Chancellors),
 *                              which happens even when the revealing player isn't the active player
 */
public record RegisterOpeningHandUpkeepTriggerEffect(CardEffect effect, boolean revealersFirstUpkeep)
        implements PregameChoiceEffect {

    public RegisterOpeningHandUpkeepTriggerEffect(CardEffect effect) {
        this(effect, false);
    }
}
