package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "116")
public class JundSojourners extends Card {

    public JundSojourners() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(DealDamageEffect.toAnyTarget(1),
                "Have Jund Sojourners deal 1 damage to any target?"));

        addTrigger(Triggers.youCycleThisCard(), new MayEffect(DealDamageEffect.toAnyTarget(1),
                "Have Jund Sojourners deal 1 damage to any target?"));
        addCycling("{2}{R}");
    }
}
