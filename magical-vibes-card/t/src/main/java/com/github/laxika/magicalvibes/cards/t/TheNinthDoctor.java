package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;

@CardRegistration(set = "WHO", collectorNumber = "148")
@CardRegistration(set = "WHO", collectorNumber = "432")
@CardRegistration(set = "WHO", collectorNumber = "560")
@CardRegistration(set = "WHO", collectorNumber = "753")
@CardRegistration(set = "WHO", collectorNumber = "1023")
@CardRegistration(set = "WHO", collectorNumber = "1151")
public class TheNinthDoctor extends Card {

    public TheNinthDoctor() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new AdditionalUpkeepStepEffect());
    }
}
