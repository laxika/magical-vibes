package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that adds {@code amount} cards to each mill event affecting an
 * opponent of this permanent's controller.
 */
public record AdditionalMillForOpponentsEffect(int amount) implements ControllerOpponentMillBonusEffect {
}
