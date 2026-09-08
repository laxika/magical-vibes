package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect;

@CardRegistration(set = "RIX", collectorNumber = "22")
public class SlaughterTheStrong extends Card {

    public SlaughterTheStrong() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect(4));
    }
}
