package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "4")
@CardRegistration(set = "MIC", collectorNumber = "42")
public class KylerSigardianEmissary extends Card {

    private static final PermanentPredicate OTHER_HUMAN = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public KylerSigardianEmissary() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(OTHER_HUMAN,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
