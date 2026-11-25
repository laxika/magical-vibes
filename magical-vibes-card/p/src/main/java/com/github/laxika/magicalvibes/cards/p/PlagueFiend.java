package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "73")
public class PlagueFiend extends Card {

    public PlagueFiend() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{2}", new DestroyTargetPermanentEffect())
                .withPrompt("Pay {2} to prevent destruction?"));
    }
}
