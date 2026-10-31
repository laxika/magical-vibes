package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "116")
public class SephirothsIntervention extends Card {

    public SephirothsIntervention() {
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(2));
    }
}
