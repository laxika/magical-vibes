package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseXValueCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "99")
@CardRegistration(set = "TLE", collectorNumber = "183")
public class WaterbendersRestoration extends Card {

    public WaterbendersRestoration() {
        addEffect(EffectSlot.SPELL, new ChooseXValueCost(0, 100));
        addEffect(EffectSlot.SPELL, WaterbendCost.x());
        targetExactlyX(TargetFilters.creatureYouControl(), 100)
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStep());
    }
}
