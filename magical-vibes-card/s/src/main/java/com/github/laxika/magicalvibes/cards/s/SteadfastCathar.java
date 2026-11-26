package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "44")
@CardRegistration(set = "SIR", collectorNumber = "46")
public class SteadfastCathar extends Card {

    public SteadfastCathar() {
        addTrigger(Triggers.attacks(), BoostEffect.self(0, 2));
    }
}
