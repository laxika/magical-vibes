package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "108")
public class InsatiableSkittermaw extends Card {

    public InsatiableSkittermaw() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new VoidCondition(),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
