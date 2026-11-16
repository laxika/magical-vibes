package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "2")
@CardRegistration(set = "40K", collectorNumber = "171")
@CardRegistration(set = "40K", collectorNumber = "178")
@CardRegistration(set = "40K", collectorNumber = "319")
public class AbaddonTheDespoiler extends Card {

    public AbaddonTheDespoiler() {
        addTrigger(Triggers.youCastSpell().duringYourTurn().castFrom(Zone.HAND)
                        .withStackEntryFilter(new StackEntryManaValueAtMostAmountPredicate(
                                new LifeLostThisTurn(CountScope.OPPONENTS)))
                        .grantedToTheSpell(),
                new CascadeEffect());
    }
}
