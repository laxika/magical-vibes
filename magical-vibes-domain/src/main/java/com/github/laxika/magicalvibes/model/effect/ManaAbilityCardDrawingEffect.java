package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a card-drawing effect that resolves inline as part of a mana ability.
 *
 * <p>Most card-drawing effects use the stack or an interaction flow. This marker identifies the
 * simple draw rider that the mana-ability executor can apply without dispatching a stack effect.
 */
public interface ManaAbilityCardDrawingEffect extends CardDrawingEffect {
}
