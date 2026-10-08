package com.github.laxika.magicalvibes.model.effect;

/** Reveals an opening-hand card and schedules its mandatory first-upkeep effect. */
public record RegisterOpeningHandUpkeepTriggerEffect(CardEffect effect) implements PregameChoiceEffect {
}
