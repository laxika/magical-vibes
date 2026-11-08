package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "1")
public class BenalishKnightCounselor extends Card {

    public BenalishKnightCounselor() {
        addTrigger(Triggers.enlists(), new GrantAdditionalPlusOnePlusOneCounterToNextCreatureSpellEffect());
    }
}
