package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Creates token(s) that are copies of the source permanent (the permanent with this ability).
 * The token copies all copiable characteristics per CR 707.2. When activated from the graveyard
 * (no source permanent), the copy is taken from the stack entry's card snapshot.
 *
 * <p>The color/subtype/mana-cost/P-T components apply the Embalm / Eternalize style "except it's a
 * &lt;color&gt; &lt;subtype&gt; ... with no mana cost" transformation to the copy; all are inert
 * ({@code null}/{@code false}) for a plain copy. Embalm keeps the source's P/T (both overrides
 * {@code null}); Eternalize sets a fixed base P/T (e.g. a 4/4 black Zombie).
 *
 * @param removeLegendary  if true, the token is not legendary (removes LEGENDARY supertype)
 * @param amount           number of token copies to create
 * @param colorOverride    if non-null, the token's color is set to exactly this color
 * @param addedSubtype     if non-null, this creature subtype is added to the copy (e.g. Zombie)
 * @param removeManaCost   if true, the token has no mana cost
 * @param powerOverride    if non-null, the token's base power is set to this (Eternalize 4/4)
 * @param toughnessOverride if non-null, the token's base toughness is set to this (Eternalize 4/4)
 * @param grantHaste       if true, the token gains haste
 * @param exileAtEndStep  if true, the token is exiled at the beginning of the next end step
 */
public record CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                            CardColor colorOverride, CardSubtype addedSubtype,
                                            boolean removeManaCost,
                                            Integer powerOverride, Integer toughnessOverride,
                                            boolean grantHaste, boolean exileAtEndStep)
        implements CardEffect {

    /** Backward-compatible: single copy, keeps legendary status, no transformation. */
    public CreateTokenCopyOfSourceEffect() {
        this(false, 1, null, null, false, null, null, false, false);
    }

    /** Backward-compatible: copies with an optional non-legendary flag and count, no transformation. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount) {
        this(removeLegendary, amount, null, null, false, null, null, false, false);
    }

    /** Plain source copy with optional haste and exile at the next end step. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         boolean grantHaste, boolean exileAtEndStep) {
        this(removeLegendary, amount, null, null, false, null, null, grantHaste, exileAtEndStep);
    }

    /** Embalm/Eternalize-style source copy with explicit power/toughness overrides. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         CardColor colorOverride, CardSubtype addedSubtype,
                                         boolean removeManaCost, Integer powerOverride,
                                         Integer toughnessOverride) {
        this(removeLegendary, amount, colorOverride, addedSubtype, removeManaCost,
                powerOverride, toughnessOverride, false, false);
    }

    /** Embalm-style: color/subtype/no-mana-cost transform, keeps the source's P/T. */
    public CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount,
                                         CardColor colorOverride, CardSubtype addedSubtype,
                                         boolean removeManaCost) {
        this(removeLegendary, amount, colorOverride, addedSubtype, removeManaCost, null, null, false, false);
    }
}
