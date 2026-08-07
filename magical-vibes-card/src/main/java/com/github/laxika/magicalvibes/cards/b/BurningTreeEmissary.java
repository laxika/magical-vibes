package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "GTC", collectorNumber = "216")
public class BurningTreeEmissary extends Card {

    public BurningTreeEmissary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.RED));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.GREEN));
    }
}
