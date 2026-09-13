package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "WWK", collectorNumber = "128")
@CardRegistration(set = "DDI", collectorNumber = "47")
@CardRegistration(set = "EMA", collectorNumber = "229")
@CardRegistration(set = "GNT", collectorNumber = "55")
public class PilgrimsEye extends Card {

    public PilgrimsEye() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                        "Search your library for a basic land card?"));
    }
}
