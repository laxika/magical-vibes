package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlledDragonAsCast;
import com.github.laxika.magicalvibes.model.condition.RevealCardFromHandCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "201")
public class ScaleguardSentinels extends Card {

    public ScaleguardSentinels() {
        addEffect(EffectSlot.SPELL, RevealCardFromHandCost.optional(
                new CardSubtypePredicate(CardSubtype.DRAGON), "Dragon"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new AnyOf(List.of(new RevealCardFromHandCostPaid(), new ControlledDragonAsCast())),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
