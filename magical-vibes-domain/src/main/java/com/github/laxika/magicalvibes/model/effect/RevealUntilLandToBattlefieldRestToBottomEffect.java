package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the caster's library until a land card is revealed. That land is
 * put onto the battlefield under the caster's control, and the rest of the revealed cards are put
 * on the bottom of the library in any order. If the library is exhausted without revealing a land,
 * all revealed cards are put on the bottom in any order.
 * <p>
 * Used by Recross the Paths (as the pre-clash body of a {@link ClashEffect}).
 */
public record RevealUntilLandToBattlefieldRestToBottomEffect() implements CardEffect {
}
