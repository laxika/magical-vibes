package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "49")
public class CovertTechnician extends Card {

    public CovertTechnician() {
        addNinjutsu("{1}{U}");

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.ARTIFACT), "artifact")
                                .boundedByEventValue(),
                        "Put an artifact card from your hand onto the battlefield?"));
    }
}
