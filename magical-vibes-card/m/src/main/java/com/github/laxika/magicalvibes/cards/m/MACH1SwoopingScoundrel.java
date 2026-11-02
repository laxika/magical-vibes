package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "662")
public class MACH1SwoopingScoundrel extends Card {

    public MACH1SwoopingScoundrel() {
        OncePerTurnTriggerEffect surveil = new OncePerTurnTriggerEffect(new SurveilEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), surveil);
        addTrigger(Triggers.youGainLife(), surveil);
    }
}
