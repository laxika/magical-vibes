package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Exile up to one target creature you own, then search your library for a card matching
 * {@code searchFilter}. Put both cards onto the battlefield under your control, then shuffle"
 * (Sword of Hearth and Home). The exiled card stays in exile while the library search is made and
 * enters the battlefield together with the found card, whether or not a card is found.
 *
 * @param searchFilter the card the library search looks for
 */
public record ExileTargetThenSearchLibraryPutBothOntoBattlefieldEffect(CardPredicate searchFilter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return FlickerEffect.flickerTargetUnderYourControl().targetSpec();
    }
}
