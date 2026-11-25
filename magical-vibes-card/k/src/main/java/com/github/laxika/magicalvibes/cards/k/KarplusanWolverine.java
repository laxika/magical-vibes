package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "87")
public class KarplusanWolverine extends Card {

    public KarplusanWolverine() {
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(DealDamageEffect.toAnyTarget(1),
                        "Have Karplusan Wolverine deal 1 damage to any target?"));
    }
}
