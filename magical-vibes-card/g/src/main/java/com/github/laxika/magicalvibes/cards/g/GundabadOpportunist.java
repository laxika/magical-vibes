package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "101")
public class GundabadOpportunist extends Card {

    public GundabadOpportunist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
