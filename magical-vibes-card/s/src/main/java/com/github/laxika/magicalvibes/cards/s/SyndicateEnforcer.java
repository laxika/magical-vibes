package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "80")
public class SyndicateEnforcer extends Card {

    public SyndicateEnforcer() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                UnlessPaysEffect.youMayPay("{W/B}", new LoseLifeEffect(1, Subjects.eachOpponent(), true))
                        .withPrompt("Pay {W/B} to extort?"));
    }
}
