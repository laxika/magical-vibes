package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "49")
public class BlueDragon extends Card {

    public BlueDragon() {
        var boostTargetCreature = BoostEffect.target(-3, 0, Duration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creatureAnOpponentControls())
                .bindEffect(boostTargetCreature);
        var boostTargetCreature2 = BoostEffect.target(-2, 0, Duration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(boostTargetCreature2);
        var boostTargetCreature3 = BoostEffect.target(-1, 0, Duration.UNTIL_YOUR_NEXT_TURN);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(boostTargetCreature3);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                boostTargetCreature,
                boostTargetCreature2,
                boostTargetCreature3);
    }
}
