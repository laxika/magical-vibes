package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "RIX", collectorNumber = "172")
public class StormFleetSprinter extends Card {

    public StormFleetSprinter() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
