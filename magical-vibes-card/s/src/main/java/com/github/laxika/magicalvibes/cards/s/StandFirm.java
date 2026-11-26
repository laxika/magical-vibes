package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "17")
@CardRegistration(set = "DDL", collectorNumber = "21")
public class StandFirm extends Card {

    public StandFirm() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 1))
                .addSpellEffect(new ScryEffect(2));
    }
}
