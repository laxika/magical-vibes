package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "211")
public class ParkHeightsPegasus extends Card {

    public ParkHeightsPegasus() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(
                new PermanentEnteredThisTurn(new CardTypePredicate(CardType.CREATURE), 2),
                new DrawCardEffect(1)));
    }
}
