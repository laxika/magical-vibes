package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of any color that a land an opponent controls could produce (the controller
 * chooses among the available colors). If only one color is available it is added automatically;
 * if no opponent land could produce colored mana, no mana is produced. Used by Fellwar Stone.
 */
public record AwardManaOfColorsOpponentLandsCouldProduceEffect() implements ManaProducingEffect {
}
