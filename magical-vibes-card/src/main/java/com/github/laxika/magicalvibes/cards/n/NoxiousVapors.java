package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect;

@CardRegistration(set = "PLS", collectorNumber = "49")
public class NoxiousVapors extends Card {

    public NoxiousVapors() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesOneCardOfEachColorThenDiscardsRestEffect());
    }
}
