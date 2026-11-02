package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "243")
@CardRegistration(set = "DDE", collectorNumber = "47")
public class DarigaazTheIgniter extends Card {

    public DarigaazTheIgniter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayPayManaEffect("{2}{R}", new DealDamageToTargetPlayerEqualToChosenColorCardsInHandEffect(),
                        "Pay {2}{R}?"));
    }
}
