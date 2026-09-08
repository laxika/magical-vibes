package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentFromHandWithManaValueEffect;

@CardRegistration(set = "KHM", collectorNumber = "177")
public class InSearchOfGreatness extends Card {

    public InSearchOfGreatness() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayCastPermanentFromHandWithManaValueEffect());
    }
}
