package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Static effect for auras that animate the enchanted permanent into a creature while remaining
 * whatever it already was. "Enchanted [permanent] is a P/T [color] [subtypes] creature that's
 * still a [its type]" — e.g. Living Terrain ("Enchanted land is a 5/6 green Treefolk creature
 * that's still a land").
 *
 * <p>The type change (adds the creature card type and the granted creature subtypes) applies in
 * layer 4; the colour and base P/T are contributed by
 * {@code EnchantedPermanentBecomesCreatureEffectHandler} in the layer-7 accumulator pass. The
 * granted card/creature types are additive: the enchanted permanent keeps its existing types
 * (a land stays a land).
 *
 * @param power    the creature's base power
 * @param toughness the creature's base toughness
 * @param color    the colour the enchanted permanent becomes (replaces its existing colours)
 * @param subtypes the creature subtypes the enchanted permanent gains (e.g. Treefolk)
 */
public record EnchantedPermanentBecomesCreatureEffect(int power, int toughness, CardColor color,
                                                      List<CardSubtype> subtypes) implements CardEffect {
}
