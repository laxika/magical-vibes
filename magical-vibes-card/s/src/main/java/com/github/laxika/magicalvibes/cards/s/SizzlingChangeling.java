package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "155")
public class SizzlingChangeling extends Card {

    public SizzlingChangeling() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(1, false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
