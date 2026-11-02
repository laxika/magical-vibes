package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "144")
@CardRegistration(set = "DBL", collectorNumber = "411")
public class BelligerentGuest extends Card {

    public BelligerentGuest() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofBloodToken(1));
    }
}
