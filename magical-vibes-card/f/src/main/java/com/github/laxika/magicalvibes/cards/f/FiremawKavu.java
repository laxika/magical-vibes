package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "153")
@CardRegistration(set = "TSR", collectorNumber = "163")
public class FiremawKavu extends Card {

    public FiremawKavu() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toTargetCreature(2))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), DealDamageEffect.toTargetCreature(4));
        addStaticEffect(new EchoEffect("{5}{R}"));
    }
}
