package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static permission to cast matching spells for a mana cost by returning a tapped creature
 * controlled by the caster to its owner's hand.
 */
public record WebSlingingEffect(String manaCost, CardPredicate filter) implements CardEffect {
}
