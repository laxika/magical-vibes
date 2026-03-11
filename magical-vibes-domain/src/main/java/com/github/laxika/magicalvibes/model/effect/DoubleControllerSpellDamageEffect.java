package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static effect that doubles damage dealt by instant or sorcery spells of the specified color
 * controlled by this permanent's controller. Unlike {@link DoubleDamageEffect} which doubles
 * all damage globally, this only affects the controller's spells of the matching color.
 * Used by Fire Servant and similar cards.
 */
public record DoubleControllerSpellDamageEffect(CardColor color) implements CardEffect {
}
