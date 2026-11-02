package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "253")
public class BladegriffPrototype extends Card {

    public BladegriffPrototype() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
