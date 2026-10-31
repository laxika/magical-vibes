package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DFT", collectorNumber = "95")
@CardRegistration(set = "SOA", collectorNumber = "31")
public class LocustSpray extends Card {

    public LocustSpray() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-1, -1));
        addCycling("{B}");
    }
}
