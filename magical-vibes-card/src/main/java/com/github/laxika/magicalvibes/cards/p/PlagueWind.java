package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesYouDontControlEffect;

@CardRegistration(set = "10E", collectorNumber = "169")
public class PlagueWind extends Card {

    public PlagueWind() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesYouDontControlEffect(true));
    }
}
