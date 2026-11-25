package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "101")
public class KnowledgeAndPower extends Card {

    public KnowledgeAndPower() {
        addTrigger(Triggers.youScry(), UnlessPaysEffect.youMayPay("{2}", DealDamageEffect.toAnyTarget(2))
                .withPrompt("Pay {2} to have Knowledge and Power deal 2 damage to any target?"));
    }
}
