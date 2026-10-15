package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PCY", collectorNumber = "123")
public class SiltCrawler extends Card {

    public SiltCrawler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
