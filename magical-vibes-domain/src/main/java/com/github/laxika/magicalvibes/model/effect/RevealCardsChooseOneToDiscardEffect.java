package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Target player reveals X cards from their hand, where X is the number of permanents you control
 * matching {@code countFilter}. You choose one of those cards. That player discards that card."
 *
 * <p>Unlike {@link ChooseCardsFromTargetHandEffect} (Duress-style: the whole hand is revealed and the
 * caster picks), here the <em>target</em> chooses which X cards to reveal (hiding the rest), and only
 * then does the caster pick one of the revealed cards to discard. Modelled as a two-phase interaction:
 * the target reveals X cards ({@code RevealCardsFromHandChoice}) — skipped when their hand is already
 * ≤ X — followed by the caster's discard pick ({@code ChooseRevealedCardToDiscardChoice}). Used by
 * Thieving Sprite with {@code PermanentHasAnySubtypePredicate(FAERIE)}.
 */
public record RevealCardsChooseOneToDiscardEffect(PermanentPredicate countFilter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
