package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "107")
@CardRegistration(set = "MMA", collectorNumber = "131")
@CardRegistration(set = "EMA", collectorNumber = "149")
@CardRegistration(set = "TSR", collectorNumber = "192")
public class Stingscourger extends Card {

    public Stingscourger() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnToHandEffect.target());
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{3}{R}"));
    }
}
