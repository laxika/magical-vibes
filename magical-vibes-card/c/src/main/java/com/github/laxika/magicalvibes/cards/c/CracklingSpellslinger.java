package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.GrantStormToNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "25")
@CardRegistration(set = "OTC", collectorNumber = "61")
public class CracklingSpellslinger extends Card {

    public CracklingSpellslinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast()),
                new GrantStormToNextInstantOrSorceryCastThisTurnEffect());
    }
}
