package com.github.laxika.magicalvibes.model.effect;

/**
 * "Pay {cost}" as the payable side of a {@link ForcedCostOrElseEffect}. Because paying mana is
 * inherently a choice, it is only meaningful with {@code optional = true} ("you may pay {cost};
 * if you don't, [penalty]"). Used by Force of Nature ("pay {G}{G}{G}{G} or take 8 damage").
 *
 * @param manaCost mana cost string like {@code "{G}{G}{G}{G}"}
 */
public record PayManaCost(String manaCost) implements CostEffect {
}
