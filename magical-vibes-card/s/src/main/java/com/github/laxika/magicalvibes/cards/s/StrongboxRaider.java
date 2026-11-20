package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "96")
public class StrongboxRaider extends Card {

    public StrongboxRaider() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                new ExileTopCardMayPlayThisTurnEffect(2, false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).choosingOne());
    }
}
