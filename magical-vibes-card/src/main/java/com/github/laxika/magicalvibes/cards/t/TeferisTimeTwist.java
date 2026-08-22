package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "72")
public class TeferisTimeTwist extends Card {

    public TeferisTimeTwist() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStepWithCounters(1));
    }
}
