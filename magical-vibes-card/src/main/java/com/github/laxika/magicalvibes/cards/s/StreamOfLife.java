package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "4ED", collectorNumber = "272")
@CardRegistration(set = "9ED", collectorNumber = "272")
@CardRegistration(set = "6ED", collectorNumber = "254")
@CardRegistration(set = "8ED", collectorNumber = "282")
@CardRegistration(set = "7ED", collectorNumber = "272")
@CardRegistration(set = "5ED", collectorNumber = "328")
public class StreamOfLife extends Card {

    public StreamOfLife() {
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(new XValue()));
    }
}
