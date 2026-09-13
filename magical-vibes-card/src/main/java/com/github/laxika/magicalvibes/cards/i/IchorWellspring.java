package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MBS", collectorNumber = "110")
@CardRegistration(set = "DDU", collectorNumber = "54")
public class IchorWellspring extends Card {

    public IchorWellspring() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
