package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepsEffect;

@CardRegistration(set = "OTJ", collectorNumber = "222")
public class ObekaSplitterOfSeconds extends Card {

    public ObekaSplitterOfSeconds() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new AdditionalUpkeepStepsEffect(new EventValue()));
    }
}
