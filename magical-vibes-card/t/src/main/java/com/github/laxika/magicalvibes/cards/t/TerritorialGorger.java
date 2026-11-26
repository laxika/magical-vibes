package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "136")
public class TerritorialGorger extends Card {

    public TerritorialGorger() {
        addTrigger(Triggers.youGetEnergy(), BoostEffect.self(2, 2));
    }
}
