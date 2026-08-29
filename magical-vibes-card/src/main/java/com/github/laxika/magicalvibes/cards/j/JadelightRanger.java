package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

@CardRegistration(set = "RIX", collectorNumber = "136")
public class JadelightRanger extends Card {

    public JadelightRanger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExploreEffect());
    }
}
