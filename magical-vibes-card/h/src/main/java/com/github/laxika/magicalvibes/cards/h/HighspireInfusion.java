package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AER", collectorNumber = "111")
@CardRegistration(set = "KLR", collectorNumber = "163")
public class HighspireInfusion extends Card {

    public HighspireInfusion() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3))
                .addSpellEffect(new EnergyCountersEffect(2));
    }
}
