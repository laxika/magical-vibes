package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "31")
@CardRegistration(set = "MSC", collectorNumber = "330")
public class GloriousPurpose extends Card {

    public GloriousPurpose() {
        addTrigger(Triggers.creatureYouControlConnives(), SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new PutCountersOnSelfEffect(CounterType.PLAN)));

        // "When the sixth plan counter is put on this enchantment" is qualified by the counter placement
        // itself, so the threshold is checked only when the counter is put, not again on resolution.
        addTrigger(Triggers.countersPutOnThis(), ConditionalEffect.atTriggerTime(
                new SourceCounterThreshold(6, CounterType.PLAN),
                new SacrificeSelfThenEffect(
                        ExileTopCardsAndMayCastSpellsEffect.controllerWithRestToHand(4))));
    }
}
