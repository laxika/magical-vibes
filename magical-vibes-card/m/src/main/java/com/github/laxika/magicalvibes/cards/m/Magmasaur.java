package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "188")
@CardRegistration(set = "TPR", collectorNumber = "141")
public class Magmasaur extends Card {

    public Magmasaur() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                "Remove a +1/+1 counter from it?",
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new MassDamageEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE), true, false,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))))));
    }
}
