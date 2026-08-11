package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "ECL", collectorNumber = "189")
public class PrismaticUndercurrents extends Card {

    public PrismaticUndercurrents() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(
                        new ColorsAmongControlledPermanents(),
                        CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.HAND));

        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
    }
}
