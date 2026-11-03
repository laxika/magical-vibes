package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "41")
public class WanderbrinePreacher extends Card {

    public WanderbrinePreacher() {
        // Whenever this creature becomes tapped, you gain 2 life.
        addTrigger(Triggers.becomesTapped(), new GainLifeEffect(2));
    }
}
