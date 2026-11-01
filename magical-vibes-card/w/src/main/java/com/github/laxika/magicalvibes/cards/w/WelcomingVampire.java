package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "46")
@CardRegistration(set = "DBL", collectorNumber = "313")
@CardRegistration(set = "TDC", collectorNumber = "140")
@CardRegistration(set = "MKC", collectorNumber = "91")
@CardRegistration(set = "LCC", collectorNumber = "140")
public class WelcomingVampire extends Card {

    public WelcomingVampire() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new EnteringCreatureMaxPowerConditionalEffect(2,
                        new OncePerTurnTriggerEffect(new DrawCardEffect(1))));
    }
}
