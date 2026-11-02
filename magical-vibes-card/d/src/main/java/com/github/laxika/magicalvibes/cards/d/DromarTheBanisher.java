package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllPermanentsOfChosenColorToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "244")
public class DromarTheBanisher extends Card {

    public DromarTheBanisher() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayPayManaEffect("{2}{U}",
                        new ReturnAllPermanentsOfChosenColorToHandEffect(new PermanentIsCreaturePredicate()),
                        "Pay {2}{U}?"));
    }
}
