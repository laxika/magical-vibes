package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnotherCreatureOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "299")
public class Lifeline extends Card {

    public Lifeline() {
        // "If another creature is on the battlefield": checked as it triggers, looking back at the creatures
        // that died with it, and again as it resolves (CR 603.4). Lifeline itself counts when it is an animated
        // creature that dies.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield()),
                new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true));
    }
}
