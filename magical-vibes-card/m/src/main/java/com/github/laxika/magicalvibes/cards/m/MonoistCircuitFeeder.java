package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "110")
public class MonoistCircuitFeeder extends Card {

    public MonoistCircuitFeeder() {
        PermanentCount artifacts = new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);

        var boostTargetCreature = new BoostTargetCreatureEffect(artifacts, new Fixed(0));
        target(TargetFilters.creatureYouControl())
                .bindEffect(boostTargetCreature);
        var boostTargetCreature2 = new BoostTargetCreatureEffect(new Fixed(0), new Scaled(artifacts, -1));
        target(TargetFilters.creatureAnOpponentControls())
                .bindEffect(boostTargetCreature2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), boostTargetCreature, boostTargetCreature2);
    }
}
