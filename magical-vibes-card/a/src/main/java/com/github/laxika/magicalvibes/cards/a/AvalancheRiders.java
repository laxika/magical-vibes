package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "74")
@CardRegistration(set = "TSB", collectorNumber = "55")
public class AvalancheRiders extends Card {

    public AvalancheRiders() {
        target(TargetFilters.land())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{3}{R}"));
    }
}
