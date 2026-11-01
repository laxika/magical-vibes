package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "109")
public class SplatterGoblin extends Card {

    public SplatterGoblin() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dies(ObjectRelation.THIS), new BoostTargetCreatureEffect(-1, -1));
    }
}
