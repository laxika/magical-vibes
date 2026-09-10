package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterRandomDiscardAtNextUpkeepEffect;

@CardRegistration(set = "TMT", collectorNumber = "88")
@CardRegistration(set = "TMT", collectorNumber = "235")
@CardRegistration(set = "TMT", collectorNumber = "286")
@CardRegistration(set = "TMT", collectorNumber = "296")
public class CaseyJonesVigilante extends Card {

    public CaseyJonesVigilante() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterRandomDiscardAtNextUpkeepEffect(3));
    }
}
