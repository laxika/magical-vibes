package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "9ED", collectorNumber = "272")
public class StreamOfLife extends Card {

    public StreamOfLife() {
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(new XValue()));
    }
}
