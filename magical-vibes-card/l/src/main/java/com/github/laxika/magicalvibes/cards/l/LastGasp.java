package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "93")
@CardRegistration(set = "RVR", collectorNumber = "79")
@CardRegistration(set = "SOS", collectorNumber = "86")
@CardRegistration(set = "UMA", collectorNumber = "103")
@CardRegistration(set = "GK1", collectorNumber = "5")
public class LastGasp extends Card {

    public LastGasp() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-3, -3));
    }
}
