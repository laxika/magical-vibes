package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "57")
public class DiseaseCarriers extends Card {

    public DiseaseCarriers() {
        // When this creature dies, target creature gets -2/-2 until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new BoostTargetCreatureEffect(-2, -2));
    }
}
