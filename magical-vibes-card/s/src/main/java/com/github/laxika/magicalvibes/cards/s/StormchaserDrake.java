package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "82")
@CardRegistration(set = "DBL", collectorNumber = "349")
public class StormchaserDrake extends Card {

    public StormchaserDrake() {
        addTrigger(Triggers.becomesTargetOfYourSpell(), new DrawCardEffect());
    }
}
