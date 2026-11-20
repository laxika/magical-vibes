package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;

@CardRegistration(set = "ECL", collectorNumber = "242")
@CardRegistration(set = "ECL", collectorNumber = "379")
public class ShadowUrchin extends Card {

    public ShadowUrchin() {
        addTrigger(Triggers.attacks(), new BlightEffect(1, null));
        addTrigger(Triggers.creatureDies(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        ObjectRelation.YOU_CONTROL),
                new ExileTopCardMayPlayThisTurnEffect(new CountersOnTriggeringPermanent(), false)
                        .until(Duration.UNTIL_YOUR_NEXT_END_STEP));
    }
}
