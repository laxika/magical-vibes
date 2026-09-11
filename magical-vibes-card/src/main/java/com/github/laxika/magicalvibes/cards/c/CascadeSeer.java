package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "ZNR", collectorNumber = "48")
public class CascadeSeer extends Card {

    public CascadeSeer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(new PartySize()));
    }
}
