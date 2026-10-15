package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromChosenSubtypeEffect;

@CardRegistration(set = "ECL", collectorNumber = "132")
@CardRegistration(set = "ECL", collectorNumber = "363")
@CardRegistration(set = "ECL", collectorNumber = "387")
@CardRegistration(set = "ECL", collectorNumber = "397")
public class CollectiveInferno extends Card {

    public CollectiveInferno() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new DoubleDamageFromChosenSubtypeEffect());
    }
}
