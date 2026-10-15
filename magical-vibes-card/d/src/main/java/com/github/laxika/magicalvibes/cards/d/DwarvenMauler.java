package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostForTargetingSourceEffect;

@CardRegistration(set = "HOB", collectorNumber = "95")
public class DwarvenMauler extends Card {

    public DwarvenMauler() {
        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostForTargetingSourceEffect(2));
    }
}
