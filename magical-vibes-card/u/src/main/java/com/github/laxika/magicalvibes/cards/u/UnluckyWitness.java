package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "128")
@CardRegistration(set = "MSC", collectorNumber = "812")
public class UnluckyWitness extends Card {

    public UnluckyWitness() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(2, false)
                .until(Duration.UNTIL_YOUR_NEXT_END_STEP).upTo(1));
    }
}
