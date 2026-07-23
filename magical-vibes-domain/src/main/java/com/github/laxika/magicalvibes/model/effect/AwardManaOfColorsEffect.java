package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * Add {@code amount} mana, each individually chosen from a fixed list of colors.
 * <p>
 * With a fixed amount of 1 this models dual/multi-color mana producers such as Manaforge Cinder
 * ("Add {B} or {R}"). With {@code amount > 1} each mana's color is chosen separately from the same
 * fixed list, modelling filter lands like Fire-Lit Thicket ("Add {R}{R}, {R}{G}, or {G}{G}") and
 * spells like Burnt Offering ("Add X mana in any combination of {B} and/or {R}" via
 * {@code XValue}). If the list holds a single color, that color is added {@code amount} times
 * without a prompt.
 */
public record AwardManaOfColorsEffect(List<ManaColor> colors, DynamicAmount amount) implements ManaProducingEffect {

    public AwardManaOfColorsEffect(List<ManaColor> colors) {
        this(colors, new Fixed(1));
    }

    public AwardManaOfColorsEffect(List<ManaColor> colors, int amount) {
        this(colors, new Fixed(amount));
    }
}
