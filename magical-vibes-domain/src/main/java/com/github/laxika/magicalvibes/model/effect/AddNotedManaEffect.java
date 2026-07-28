package com.github.laxika.magicalvibes.model.effect;

/**
 * Mana ability that adds one mana of the source permanent's last noted mana type (see
 * {@link NoteManaSpentForActivationEffect}), with no restriction on how it may be spent
 * (Jeweled Amulet). Adds nothing when the source has no noted mana.
 */
public record AddNotedManaEffect() implements CardEffect, ManaProducingEffect {
}
