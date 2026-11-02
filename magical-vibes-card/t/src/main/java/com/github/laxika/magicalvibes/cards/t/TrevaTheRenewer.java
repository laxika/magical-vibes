package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifePerPermanentOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "280")
@CardRegistration(set = "DDE", collectorNumber = "49")
public class TrevaTheRenewer extends Card {

    public TrevaTheRenewer() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayPayManaEffect("{2}{W}", new GainLifePerPermanentOfChosenColorEffect(), "Pay {2}{W}?"));
    }
}
