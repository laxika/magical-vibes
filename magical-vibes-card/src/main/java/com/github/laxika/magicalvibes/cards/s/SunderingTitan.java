package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseLandOfEachBasicTypeThenDestroyEffect;

@CardRegistration(set = "DST", collectorNumber = "146")
public class SunderingTitan extends Card {

    public SunderingTitan() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseLandOfEachBasicTypeThenDestroyEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ChooseLandOfEachBasicTypeThenDestroyEffect());
    }
}
