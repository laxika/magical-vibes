package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;

@CardRegistration(set = "MSC", collectorNumber = "31")
@CardRegistration(set = "MSC", collectorNumber = "330")
public class GloriousPurpose extends Card {

    public GloriousPurpose() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_CONNIVES, SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new PutCountersOnSelfEffect(CounterType.PLAN)));

        addEffect(EffectSlot.ON_SELF_COUNTERS_PUT, new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.PLAN),
                SacrificeSelfThenEffect.reflexive(
                        ExileTopCardsAndMayCastSpellsEffect.controllerWithRestToHand(4))));
    }
}
