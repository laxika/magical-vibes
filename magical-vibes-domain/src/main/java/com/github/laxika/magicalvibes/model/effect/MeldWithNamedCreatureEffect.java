package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile this permanent and a creature you both own and control named {@code partnerName}, then
 * meld them into this card's back face (the meld result). Used by Gisela, the Broken Blade.
 *
 * <p>Intervening-if ownership/control is checked by the wrapping {@code ConditionalEffect};
 * this effect re-validates at resolution and no-ops if the partner or source is gone / not owned.
 */
public record MeldWithNamedCreatureEffect(String partnerName) implements CardEffect {
}
