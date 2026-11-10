package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySnowManaOfItsColorSpentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "163")
public class BorealOutrider extends Card {

    public BorealOutrider() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                        .withStackEntryFilter(new StackEntrySnowManaOfItsColorSpentPredicate()),
                new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(new Fixed(1)));
    }
}
