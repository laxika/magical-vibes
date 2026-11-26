package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "78")
public class OrcSureshot extends Card {

    public OrcSureshot() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                        BoostEffect.target(-1, -1));
    }
}
