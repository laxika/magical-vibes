package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "RNA", collectorNumber = "49")
public class SagesRowSavant extends Card {

    public SagesRowSavant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
    }
}
