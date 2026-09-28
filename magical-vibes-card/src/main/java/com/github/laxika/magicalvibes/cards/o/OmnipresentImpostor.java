package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HasAllCardNamesEffect;

@CardRegistration(set = "MB2", collectorNumber = "268")
@CardRegistration(set = "MB2", collectorNumber = "504")
public class OmnipresentImpostor extends Card {

    public OmnipresentImpostor() {
        addEffect(EffectSlot.STATIC, new HasAllCardNamesEffect());
    }
}
