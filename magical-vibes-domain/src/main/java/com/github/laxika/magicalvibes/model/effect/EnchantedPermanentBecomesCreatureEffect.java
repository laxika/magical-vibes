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
 * <p>When {@code powerToughnessEqualsManaValue} is {@code true} the fixed {@code power}/{@code
 * toughness} are ignored and the creature's base P/T are each set to the enchanted permanent's
 * mana value; additionally the animation only applies while the enchanted permanent isn't already
 * a creature (Animate Artifact — "As long as enchanted artifact isn't a creature, it's an artifact
 * creature with power and toughness each equal to its mana value").
 *
 * @param power    the creature's base power (ignored when {@code powerToughnessEqualsManaValue})
 * @param toughness the creature's base toughness (ignored when {@code powerToughnessEqualsManaValue})
 * @param color    the colour the enchanted permanent becomes (replaces its existing colours)
 * @param subtypes the creature subtypes the enchanted permanent gains (e.g. Treefolk)
 * @param powerToughnessEqualsManaValue base P/T track the enchanted permanent's mana value, and the
 *                 animation only applies while it isn't already a creature
 */
public record EnchantedPermanentBecomesCreatureEffect(int power, int toughness, CardColor color,
                                                      List<CardSubtype> subtypes,
                                                      boolean powerToughnessEqualsManaValue) implements CardEffect {

    /** Fixed-P/T variant (Living Terrain). */
    public EnchantedPermanentBecomesCreatureEffect(int power, int toughness, CardColor color,
                                                   List<CardSubtype> subtypes) {
        this(power, toughness, color, subtypes, false);
    }
}
