package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source permanent to be sacrificed at the beginning of the next end step
 * (e.g. Brackwater Elemental's "When this creature attacks or blocks, sacrifice it at the beginning
 * of the next end step"). Operates on the source, so it carries no target. Sacrifice, not
 * destruction (ignores indestructible/regeneration).
 */
public record SacrificeSelfAtEndStepEffect() implements CardEffect {
}
