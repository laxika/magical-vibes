package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayerWithLowestLifeGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "MRD", collectorNumber = "13")
public class LoxodonPeacekeeper extends Card {

    public LoxodonPeacekeeper() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PlayerWithLowestLifeGainsControlOfSourceCreatureEffect());
    }
}
