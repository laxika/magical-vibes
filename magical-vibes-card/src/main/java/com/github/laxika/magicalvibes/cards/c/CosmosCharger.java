package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.ForetellCostReductionEffect;

@CardRegistration(set = "KHM", collectorNumber = "51")
public class CosmosCharger extends Card {

    public CosmosCharger() {
        addEffect(EffectSlot.STATIC, new ForetellCostReductionEffect(1, true));
        addCastingOption(new ForetellCast("{2}{U}"));
    }
}
