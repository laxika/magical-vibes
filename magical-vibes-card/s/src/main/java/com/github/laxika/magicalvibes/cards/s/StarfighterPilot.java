package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "38")
public class StarfighterPilot extends Card {

    public StarfighterPilot() {
        addTrigger(Triggers.becomesTapped(), new SurveilEffect(1));
    }
}
