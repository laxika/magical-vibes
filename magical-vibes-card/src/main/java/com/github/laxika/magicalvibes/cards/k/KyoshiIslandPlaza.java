package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "184")
public class KyoshiIslandPlaza extends Card {

    public KyoshiIslandPlaza() {
        PermanentCount shrinesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(shrinesYouControl, CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SHRINE),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
