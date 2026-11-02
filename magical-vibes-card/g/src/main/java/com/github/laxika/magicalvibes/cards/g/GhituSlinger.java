package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "77")
@CardRegistration(set = "EMA", collectorNumber = "133")
public class GhituSlinger extends Card {

    public GhituSlinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(2));
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{2}{R}"));
    }
}
