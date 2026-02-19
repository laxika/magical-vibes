package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "73")
public class Clone extends Card {

    public Clone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyCreatureOnEnterEffect());
    }
}
