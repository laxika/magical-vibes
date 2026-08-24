package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for Enduring Angel: if its controller's life total would be reduced
 * to 0 or less, the Angel transforms and that player's life total becomes 3.
 */
public record EnduringAngelLifeTotalReplacementEffect() implements CardEffect {
}
