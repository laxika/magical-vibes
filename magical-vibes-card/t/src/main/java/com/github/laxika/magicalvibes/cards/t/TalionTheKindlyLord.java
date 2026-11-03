package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "215")
@CardRegistration(set = "WOE", collectorNumber = "301")
public class TalionTheKindlyLord extends Card {

    public TalionTheKindlyLord() {
        addStaticEffect(new ChooseNumberOnEnterEffect(1, 10));
        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate()),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER),
                new DrawCardEffect(1));
    }
}
