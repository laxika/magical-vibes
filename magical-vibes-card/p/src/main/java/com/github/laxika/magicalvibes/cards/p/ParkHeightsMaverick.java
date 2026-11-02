package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "63")
@CardRegistration(set = "NCC", collectorNumber = "163")
public class ParkHeightsMaverick extends Card {

    public ParkHeightsMaverick() {
        addStaticEffect(new CanBeBlockedOnlyByFilterEffect(
                new PermanentPowerAtLeastPredicate(3),
                "creatures with power 3 or greater"
        ));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ProliferateEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ProliferateEffect());
    }
}
