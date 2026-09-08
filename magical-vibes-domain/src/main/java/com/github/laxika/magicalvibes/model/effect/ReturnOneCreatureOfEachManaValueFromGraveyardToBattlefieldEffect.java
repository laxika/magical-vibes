package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Returns one creature card for each listed mana value from the controller's graveyard to the
 * battlefield. Values are processed in order; a missing value is skipped, and multiple matching
 * cards create a mandatory sequential graveyard choice.
 *
 * @param manaValues the mana values to process in order
 */
public record ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect(
        List<Integer> manaValues
) implements CardEffect {
}
