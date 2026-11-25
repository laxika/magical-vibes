package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "24")
public class SoulCharmer extends Card {

    public SoulCharmer() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{2}", new GainLifeEffect(2))
                .withPrompt("Pay {2} to prevent gaining 2 life?"));
    }
}
