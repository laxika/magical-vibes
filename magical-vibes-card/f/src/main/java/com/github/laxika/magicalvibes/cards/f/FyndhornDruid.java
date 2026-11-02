package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceWasBlockedThisTurn;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "90a")
@CardRegistration(set = "ALL", collectorNumber = "90b")
public class FyndhornDruid extends Card {

    public FyndhornDruid() {
        // When this creature dies, if it was blocked this turn, you gain 4 life. The intervening-if
        // reads the turn-scoped "was blocked" set, so it still answers after the Druid has died.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceWasBlockedThisTurn()),
                new GainLifeEffect(4));
    }
}
