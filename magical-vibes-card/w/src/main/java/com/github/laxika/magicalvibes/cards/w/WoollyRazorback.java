package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "25")
public class WoollyRazorback extends Card {

    public WoollyRazorback() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.ICE, new Fixed(3)));

        SourceCounterThreshold hasIceCounter = new SourceCounterThreshold(1, CounterType.ICE);
        addStaticEffect(new ConditionalEffect(hasIceCounter,
                        new GrantKeywordEffect(Keyword.DEFENDER, Subjects.source())));
        addStaticEffect(new ConditionalEffect(hasIceCounter,
                PreventDamageToRecipientsEffect.dealtBy(new PermanentIsSourcePermanentPredicate()).combatOnly()));

        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                RemoveCountersEffect.fromSelf(CounterType.ICE, 1));
    }
}
