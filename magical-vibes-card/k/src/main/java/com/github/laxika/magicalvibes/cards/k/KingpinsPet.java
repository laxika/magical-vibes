package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "173")
public class KingpinsPet extends Card {

    public KingpinsPet() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                UnlessPaysEffect.youMayPay("{W/B}", new LoseLifeEffect(1, Subjects.eachOpponent(), true))
                        .withPrompt("Pay {W/B} to extort?"));
    }
}
