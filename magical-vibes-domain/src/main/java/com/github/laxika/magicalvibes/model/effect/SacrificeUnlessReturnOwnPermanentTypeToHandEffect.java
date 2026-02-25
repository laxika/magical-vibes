package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * When this permanent enters the battlefield, sacrifice it unless you return
 * a permanent of the specified type you control to its owner's hand.
 * Used by cards like Glint Hawk (return artifact) and similar "ETB bounce-or-sacrifice" creatures.
 */
public record SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType permanentType) implements CardEffect {
}
