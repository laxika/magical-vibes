package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OneOrMoreCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HOB", collectorNumber = "73")
public class GreatFierceBee extends Card {

    public GreatFierceBee() {
        // Whenever one or more other creatures die, scry 1.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(new ScryEffect(1)));
    }
}
