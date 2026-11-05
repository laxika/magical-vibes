package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "49")
public class BlueDragon extends Card {

    public BlueDragon() {
        var boostTargetCreature = new BoostTargetCreatureEffect(-3, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creatureAnOpponentControls())
                .bindEffect(boostTargetCreature);
        var boostTargetCreature2 = new BoostTargetCreatureEffect(-2, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(boostTargetCreature2);
        var boostTargetCreature3 = new BoostTargetCreatureEffect(-1, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(boostTargetCreature3);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                boostTargetCreature,
                boostTargetCreature2,
                boostTargetCreature3);
    }
}
