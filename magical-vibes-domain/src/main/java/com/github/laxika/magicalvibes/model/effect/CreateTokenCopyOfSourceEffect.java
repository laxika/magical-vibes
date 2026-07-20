package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Creates token(s) that are copies of the source permanent (the permanent with this ability).
 * The token copies all copiable characteristics per CR 707.2. When activated from the graveyard
 * (no source permanent), the copy is taken from the stack entry's card snapshot.
 *
 * <p>The last three components apply the Embalm / Eternalize style "except it's a &lt;color&gt;
 * &lt;subtype&gt; ... with no mana cost" transformation to the copy; all are inert
 * ({@code null}/{@code false}) for a plain copy.
 *
 * @param removeLegendary if true, the token is not legendary (removes LEGENDARY supertype)
 * @param amount          number of token copies to create
 * @param colorOverride   if non-null, the token's color is set to exactly this color
 * @param addedSubtype    if non-null, this creature subtype is added to the copy (e.g. Zombie)
 * @param removeManaCost  if true, the token has no mana cost
 */
public record CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                            CardColor colorOverride, CardSubtype addedSubtype,
                                            boolean removeManaCost) implements CardEffect {

    /** Backward-compatible: single copy, keeps legendary status, no transformation. */
    public CreateTokenCopyOfSourceEffect() {
        this(false, 1, null, null, false);
    }

    /** Backward-compatible: copies with an optional non-legendary flag and count, no transformation. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount) {
        this(removeLegendary, amount, null, null, false);
    }
}
