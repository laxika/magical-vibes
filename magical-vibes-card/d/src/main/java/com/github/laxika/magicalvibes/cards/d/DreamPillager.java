package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "25")
@CardRegistration(set = "AFC", collectorNumber = "125")
@CardRegistration(set = "SCD", collectorNumber = "139")
@CardRegistration(set = "CM2", collectorNumber = "93")
public class DreamPillager extends Card {

    public DreamPillager() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardsMayCastMatchingThisTurnEffect(
                        new EventValue(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
