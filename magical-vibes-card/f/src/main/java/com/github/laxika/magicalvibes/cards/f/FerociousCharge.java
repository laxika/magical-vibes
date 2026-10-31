package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "88")
public class FerociousCharge extends Card {

    public FerociousCharge() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(4, 4))
                .addSpellEffect(new ScryEffect(2));
    }
}
