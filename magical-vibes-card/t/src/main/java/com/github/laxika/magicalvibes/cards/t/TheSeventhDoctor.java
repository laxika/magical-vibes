package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TheSeventhDoctorEffect;

@CardRegistration(set = "WHO", collectorNumber = "158")
@CardRegistration(set = "WHO", collectorNumber = "442")
@CardRegistration(set = "WHO", collectorNumber = "558")
@CardRegistration(set = "WHO", collectorNumber = "763")
@CardRegistration(set = "WHO", collectorNumber = "1033")
@CardRegistration(set = "WHO", collectorNumber = "1149")
public class TheSeventhDoctor extends Card {

    public TheSeventhDoctor() {
        addEffect(EffectSlot.ON_ATTACK, new TheSeventhDoctorEffect());
    }
}
