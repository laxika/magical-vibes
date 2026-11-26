package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "90")
@CardRegistration(set = "KHM", collectorNumber = "352")
public class DreamDevourer extends Card {

    public DreamDevourer() {
        addStaticEffect(new GrantForetellToNonlandCardsInHandEffect());
        addTrigger(Triggers.youForetell(), BoostEffect.self(2, 0));
    }
}
