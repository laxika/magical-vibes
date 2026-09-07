package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "TOR", collectorNumber = "18")
public class TerohsFaithful extends Card {

    public TerohsFaithful() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));
    }
}
