package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TheSeventhDoctorEffect;

@CardRegistration(set = "WHO", collectorNumber = "158")
public class TheSeventhDoctor extends Card {

    public TheSeventhDoctor() {
        addEffect(EffectSlot.ON_ATTACK, new TheSeventhDoctorEffect());
    }
}
