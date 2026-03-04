package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect that grants a card type to permanents matching the given scope.
 * For equipment: "equipped creature is an artifact in addition to its other types" adds the type.
 * For auras: "enchanted creature is a [type]" similarly.
 *
 * @param cardType the card type to grant
 * @param scope    which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, etc.)
 */
public record GrantCardTypeEffect(CardType cardType, GrantScope scope) implements CardEffect {
}
