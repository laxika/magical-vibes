package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "KTK", collectorNumber = "91")
public class SultaiScavenger extends Card {

    public SultaiScavenger() {
        addEffect(EffectSlot.SPELL, new DelveCost());
    }
}
