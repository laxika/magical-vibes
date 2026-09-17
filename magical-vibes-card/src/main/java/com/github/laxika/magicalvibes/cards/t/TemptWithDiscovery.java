package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TemptingOfferSearchLibraryForLandToBattlefieldEffect;

@CardRegistration(set = "C13", collectorNumber = "174")
public class TemptWithDiscovery extends Card {

    public TemptWithDiscovery() {
        addEffect(EffectSlot.SPELL, new TemptingOfferSearchLibraryForLandToBattlefieldEffect());
    }
}
