package com.github.laxika.magicalvibes.model.effect;

/**
 * For each creature, its controller sacrifices a permanent unless they pay the given mana cost.
 * The affected players make their choices in active-player order, then the selected payments and
 * sacrifices are applied together.
 */
public record EachCreatureControllerSacrificesPermanentUnlessPaysEffect(String manaCost)
        implements CardEffect {
}
