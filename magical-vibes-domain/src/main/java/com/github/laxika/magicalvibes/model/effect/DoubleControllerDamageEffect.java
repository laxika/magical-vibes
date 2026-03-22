package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that doubles all damage dealt by sources controlled by this permanent's controller.
 * Unlike {@link DoubleDamageEffect} which doubles all damage globally, this only affects
 * sources controlled by the same player who controls this permanent.
 * Unlike {@link DoubleControllerSpellDamageEffect} which only affects instant/sorcery spells,
 * this affects all sources including creatures (combat damage), abilities, and spells.
 * Used by Angrath's Marauders and similar cards.
 */
public record DoubleControllerDamageEffect() implements CardEffect {
}
