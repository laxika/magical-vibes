package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "17")
@CardRegistration(set = "AKR", collectorNumber = "29")
public class OketrasAvenger extends Card {

    public OketrasAvenger() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), PreventDamageEffect.allCombatToSelf());
    }
}
