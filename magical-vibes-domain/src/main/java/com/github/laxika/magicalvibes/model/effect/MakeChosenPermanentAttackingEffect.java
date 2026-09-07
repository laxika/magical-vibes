package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Prompts for a player or planeswalker and makes the chosen permanent attack that object.
 */
public record MakeChosenPermanentAttackingEffect(UUID permanentId) implements CardEffect {
}
