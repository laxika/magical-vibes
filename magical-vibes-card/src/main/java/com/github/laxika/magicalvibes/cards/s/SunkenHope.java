package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "112")
public class SunkenHope extends Card {

    public SunkenHope() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new BounceOwnCreatureOnUpkeepEffect());
    }
}
