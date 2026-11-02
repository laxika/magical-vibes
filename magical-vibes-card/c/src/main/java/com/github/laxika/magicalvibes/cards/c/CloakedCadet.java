package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "192")
@CardRegistration(set = "DBL", collectorNumber = "459")
public class CloakedCadet extends Card {

    public CloakedCadet() {
        addTrigger(Triggers.plusOneCountersPutOnHumanYouControl(),
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));
    }
}
