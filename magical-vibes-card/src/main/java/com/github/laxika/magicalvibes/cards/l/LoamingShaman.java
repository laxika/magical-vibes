package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "M20", collectorNumber = "180")
@CardRegistration(set = "DIS", collectorNumber = "87")
@CardRegistration(set = "RVR", collectorNumber = "149")
@CardRegistration(set = "C15", collectorNumber = "190")
public class LoamingShaman extends Card {

    public LoamingShaman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, Integer.MAX_VALUE));
    }
}
