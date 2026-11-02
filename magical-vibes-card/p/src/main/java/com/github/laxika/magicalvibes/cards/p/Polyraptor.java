package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "144")
@CardRegistration(set = "SLD", collectorNumber = "1391")
@CardRegistration(set = "SLD", collectorNumber = "2194")
@CardRegistration(set = "SPG", collectorNumber = "12")
public class Polyraptor extends Card {

    public Polyraptor() {
        addTrigger(Triggers.dealtDamage(), new CreateTokenCopyOfSourceEffect());
    }
}
