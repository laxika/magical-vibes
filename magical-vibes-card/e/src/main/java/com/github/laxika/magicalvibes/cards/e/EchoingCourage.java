package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "74")
@CardRegistration(set = "MMA", collectorNumber = "143")
@CardRegistration(set = "A25", collectorNumber = "166")
public class EchoingCourage extends Card {

    public EchoingCourage() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureAndAllWithSameNameEffect(2, 2));
    }
}
