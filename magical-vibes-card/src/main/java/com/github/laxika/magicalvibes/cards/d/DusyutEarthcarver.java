package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "141")
public class DusyutEarthcarver extends Card {

    public DusyutEarthcarver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EndureEffect(3));
    }
}
