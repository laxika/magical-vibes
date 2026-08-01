package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * When this permanent enters the battlefield, sacrifice it unless you return
 * a permanent of the specified type you control to its owner's hand.
 * Used by cards like Glint Hawk (return artifact) and similar "ETB bounce-or-sacrifice" creatures.
 *
 * <p>{@code excludeSource} models the "another" wording (Faerie Impostor — "return another creature
 * you control"): the source permanent itself is never a legal return choice, so a lone matching
 * permanent that is the source means the payment is impossible and the source is sacrificed.
 */
public record SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType permanentType, boolean excludeSource) implements CardEffect {

    public SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType permanentType) {
        this(permanentType, false);
    }
}
