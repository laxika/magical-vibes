package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Target player reveals X cards from their hand. You choose one of those cards. That player
 * discards that card."
 *
 * <p>Unlike {@link ChooseCardsFromTargetHandEffect} (Duress-style: the whole hand is revealed and the
 * caster picks), here the <em>target</em> chooses which X cards to reveal (hiding the rest), and only
 * then does the caster pick one of the revealed cards to discard. Uses the shared
 * {@code RevealCardsDiscardChoice} interaction (same path as Blackmail / Noggin Whack). The
 * predicate constructor is used by Thieving Sprite; the dynamic constructor supports effects such
 * as Disciple of Phenax, where X is a mana-symbol count rather than a permanent count.
 */
public record RevealCardsChooseOneToDiscardEffect(DynamicAmount count) implements CardEffect {

    public RevealCardsChooseOneToDiscardEffect(PermanentPredicate countFilter) {
        this(new PermanentCount(countFilter, CountScope.CONTROLLER));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
