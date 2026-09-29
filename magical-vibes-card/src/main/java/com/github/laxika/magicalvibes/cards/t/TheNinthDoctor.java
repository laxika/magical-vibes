package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;

@CardRegistration(set = "WHO", collectorNumber = "148")
public class TheNinthDoctor extends Card {

    public TheNinthDoctor() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, new AdditionalUpkeepStepEffect());
    }
}
