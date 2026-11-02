package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MaxSpeedFreeFirstUnearthEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantUnearthToTargetCreatureCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "22")
public class HighwayReaver extends Card {

    public HighwayReaver() {
        addStaticEffect(new MaxSpeedFreeFirstUnearthEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyGrantUnearthToTargetCreatureCardEffect());
        addTrigger(Triggers.attacks(),
                new PerpetuallyGrantUnearthToTargetCreatureCardEffect());
    }
}
