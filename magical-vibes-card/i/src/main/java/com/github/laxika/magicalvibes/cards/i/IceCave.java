package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "24")
public class IceCave extends Card {

    public IceCave() {
        addTrigger(Triggers.playerCastsSpell(),
                new CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect());
    }
}
