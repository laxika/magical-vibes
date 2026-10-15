package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "40K", collectorNumber = "23")
public class HeraldsOfTzeentch extends Card {

    public HeraldsOfTzeentch() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
