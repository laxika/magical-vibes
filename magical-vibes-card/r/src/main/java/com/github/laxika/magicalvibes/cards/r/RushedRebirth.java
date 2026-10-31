package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DyingPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "228")
public class RushedRebirth extends Card {

    public RushedRebirth() {
        target(TargetFilters.creature())
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                new ManaValueBound(new DyingPermanentManaValue(), false, -1))));
    }
}
