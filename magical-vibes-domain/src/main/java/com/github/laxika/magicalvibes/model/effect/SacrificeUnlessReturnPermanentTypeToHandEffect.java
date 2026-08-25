package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * When this permanent enters the battlefield, sacrifice it unless you return
 * a permanent of the specified type to its owner's hand.
 */
public record SacrificeUnlessReturnPermanentTypeToHandEffect(CardType permanentType) implements CardEffect {
}
