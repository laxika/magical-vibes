package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "46")
public class Necravolver extends Card {

    public Necravolver() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{G}"));
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.singlePayment(List.of("{W}")));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Kicked(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new RepeatedAdditionalCostPaid("{W}"),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new RepeatedAdditionalCostPaid("{W}"),
                new GrantEffectToSourceEffect(EffectSlot.ON_SELF_DEALS_DAMAGE,
                        new GainLifeEffect(new EventValue()))));
    }
}
