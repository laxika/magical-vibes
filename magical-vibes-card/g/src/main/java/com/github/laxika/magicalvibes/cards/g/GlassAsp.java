package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseLifeAtNextDrawStepUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "197")
public class GlassAsp extends Card {

    public GlassAsp() {
        // Whenever this creature deals combat damage to a player, that player loses 2 life at the
        // beginning of their next draw step unless they pay {2} before that step.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new RegisterLoseLifeAtNextDrawStepUnlessPaysEffect(2, 2));
    }
}
