package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "77")
public class WhisperingSpecter extends Card {

    public WhisperingSpecter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect(),
                        "You may sacrifice it. If you do, that player discards a card for each poison counter they have."));
    }
}
