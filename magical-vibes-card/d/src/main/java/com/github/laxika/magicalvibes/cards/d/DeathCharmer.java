package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "61")
public class DeathCharmer extends Card {

    public DeathCharmer() {
        // Whenever this creature deals combat damage to a creature, that creature's controller loses
        // 2 life unless they pay {2}. The damaged creature is baked as targetId by
        // Triggers.dealsCombatDamageToCreature() (non-targeting).
        addTrigger(Triggers.dealsCombatDamageToCreature(), UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{2}", new LoseLifeEffect(2, Subjects.controllerOf(Subjects.target())))
                .withPrompt("Pay {2} to prevent losing 2 life?"));
    }
}
