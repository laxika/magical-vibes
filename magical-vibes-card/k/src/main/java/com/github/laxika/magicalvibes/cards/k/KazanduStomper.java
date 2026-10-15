package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNControlledPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "191")
public class KazanduStomper extends Card {

    public KazanduStomper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnUpToNControlledPermanentsToHandEffect(2, new PermanentIsLandPredicate(), "land"));
    }
}
