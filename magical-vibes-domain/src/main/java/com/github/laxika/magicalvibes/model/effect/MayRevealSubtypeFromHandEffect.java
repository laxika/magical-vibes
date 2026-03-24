package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "You may reveal a [subtype] card from your hand. If you do, [thenEffect]."
 * <p>
 * At trigger time the engine checks whether the controller's hand contains a card
 * with the specified subtype.  If so, a {@link MayEffect} wrapping {@code thenEffect}
 * is queued; otherwise the ability silently does nothing (the player cannot reveal
 * what they don't have).
 */
public record MayRevealSubtypeFromHandEffect(CardSubtype subtype, CardEffect thenEffect, String prompt) implements CardEffect {
}
