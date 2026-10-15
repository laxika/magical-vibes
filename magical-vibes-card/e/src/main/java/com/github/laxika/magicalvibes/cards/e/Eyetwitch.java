package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;

@CardRegistration(set = "STX", collectorNumber = "70")
public class Eyetwitch extends Card {

    public Eyetwitch() {
        addEffect(EffectSlot.ON_DEATH, new LearnEffect());
    }
}
