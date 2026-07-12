package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "SHM", collectorNumber = "120")
public class HungrySpriggan extends Card {

    public HungrySpriggan() {
        // Trample (keyword auto-loaded from Scryfall)
        // Whenever Hungry Spriggan attacks, it gets +3/+3 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(3, 3));
    }
}
