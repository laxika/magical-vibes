package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerAtLeastChosenNumberEffect;

@CardRegistration(set = "WOE", collectorNumber = "13")
@CardRegistration(set = "TDC", collectorNumber = "115")
@CardRegistration(set = "WOE", collectorNumber = "324")
@CardRegistration(set = "WOE", collectorNumber = "381")
public class ExpelTheInterlopers extends Card {

    public ExpelTheInterlopers() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesWithPowerAtLeastChosenNumberEffect());
    }
}
