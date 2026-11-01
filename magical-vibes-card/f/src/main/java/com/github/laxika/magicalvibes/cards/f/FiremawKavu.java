package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "153")
@CardRegistration(set = "TSR", collectorNumber = "163")
public class FiremawKavu extends Card {

    public FiremawKavu() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(2))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new DealDamageToTargetCreatureEffect(4));
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{5}{R}"));
    }
}
