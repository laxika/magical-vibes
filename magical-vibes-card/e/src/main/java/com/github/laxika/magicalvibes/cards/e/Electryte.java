package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "183")
public class Electryte extends Card {

    public Electryte() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToDefendingPlayerCreaturesEffect(new SourcePower(), new PermanentIsBlockingPredicate()));
    }
}
