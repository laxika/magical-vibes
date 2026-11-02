package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "11")
@CardRegistration(set = "MOM", collectorNumber = "344")
public class DuskLegionDuelist extends Card {

    public DuskLegionDuelist() {
        addTrigger(Triggers.plusOneCountersPutOnThis(),
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
