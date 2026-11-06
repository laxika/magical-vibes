package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "708")
@CardRegistration(set = "CMM", collectorNumber = "748")
public class OmarthisGhostfireInitiate extends Card {

    public OmarthisGhostfireInitiate() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(new PermanentIsColorlessPredicate()),
                new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Omarthis?"));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ManifestTopCardEffect(new CountersOnTriggeringPermanent()));
    }
}
