package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "457")
public class AmarantCoral extends Card {

    public AmarantCoral() {
        // Amarant Coral attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // Whenever Amarant Coral deals combat damage to an opponent, it deals that much damage to
        // each other opponent.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OTHER_OPPONENT));
    }
}
