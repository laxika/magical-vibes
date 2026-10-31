package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "124")
public class BountyOfMight extends Card {

    public BountyOfMight() {
        setAllowSharedTargets(true);

        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3));
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3));
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3));
    }
}
