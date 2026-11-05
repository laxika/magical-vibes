package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "81")
public class GurmagRakshasa extends Card {

    public GurmagRakshasa() {
        var boostTargetCreature = new BoostTargetCreatureEffect(-2, -2);
        target(TargetFilters.creatureAnOpponentControls())
                .bindEffect(boostTargetCreature);

        var boostTargetCreature2 = new BoostTargetCreatureEffect(2, 2);
        target(TargetFilters.creatureYouControl())
                .bindEffect(boostTargetCreature2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), boostTargetCreature, boostTargetCreature2);
    }
}
