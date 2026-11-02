package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "101")
public class KnowledgeAndPower extends Card {

    public KnowledgeAndPower() {
        addTrigger(Triggers.youScry(), new MayPayManaEffect(
                "{2}",
                new DealDamageToAnyTargetEffect(2),
                "Pay {2} to have Knowledge and Power deal 2 damage to any target?"));
    }
}
