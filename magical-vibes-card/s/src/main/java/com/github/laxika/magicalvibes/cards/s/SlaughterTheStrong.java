package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect;

@CardRegistration(set = "RIX", collectorNumber = "22")
@CardRegistration(set = "TDC", collectorNumber = "131")
public class SlaughterTheStrong extends Card {

    public SlaughterTheStrong() {
        addSpellEffect(new EachPlayerChoosesCreaturesWithTotalPowerAtMostThenSacrificeRestEffect(4));
    }
}
