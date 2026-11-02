package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "PTK", collectorNumber = "109")
@CardRegistration(set = "ME3", collectorNumber = "96")
public class DongZhouTheTyrant extends Card {

    public DongZhouTheTyrant() {
        // When Dong Zhou enters, target creature an opponent controls deals damage equal to its
        // power to that player.
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new TargetCreatureDealsPowerDamageToControllerEffect());
    }
}
