package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect;

@CardRegistration(set = "TLA", collectorNumber = "15")
public class DestinedConfrontation extends Card {

    public DestinedConfrontation() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect(4));
    }
}
