package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "237")
@CardRegistration(set = "5DN", collectorNumber = "79")
@CardRegistration(set = "PD2", collectorNumber = "4")
public class SparkElemental extends Card {

    public SparkElemental() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new SacrificeSelfEffect());
    }
}
