package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability for a static effect that grants a card type to matching cards owned by its
 * controller, including cards outside the battlefield.
 */
public interface OwnCardTypeGrantingEffect extends CardEffect {

    CardType cardType();

    CardPredicate filter();
}
