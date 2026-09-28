package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "NCC", collectorNumber = "318")
public class TreeshakerChimera extends Card {

    public TreeshakerChimera() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(3));
    }
}
