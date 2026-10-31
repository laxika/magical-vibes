package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OANA", collectorNumber = "46")
@CardRegistration(set = "ANB", collectorNumber = "94")
public class FeralRoar extends Card {

    public FeralRoar() {
        // Target creature gets +4/+4 until end of turn.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(4, 4));
    }
}
