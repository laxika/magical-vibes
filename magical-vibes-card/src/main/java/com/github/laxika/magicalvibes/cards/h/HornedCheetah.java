package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "INV", collectorNumber = "251")
public class HornedCheetah extends Card {

    public HornedCheetah() {
        // Whenever this creature deals damage, you gain that much life.
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
