package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "340")
public class Onulet extends Card {

    public Onulet() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(2));
    }
}
