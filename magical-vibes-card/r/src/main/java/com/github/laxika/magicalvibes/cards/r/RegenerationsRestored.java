package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;

@CardRegistration(set = "WHO", collectorNumber = "151")
@CardRegistration(set = "WHO", collectorNumber = "756")
@CardRegistration(set = "WHO", collectorNumber = "435")
@CardRegistration(set = "WHO", collectorNumber = "1026")
public class RegenerationsRestored extends Card {

    public RegenerationsRestored() {
        // This enchantment enters with twelve time counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(12)));

        // Vanishing's "when the last is removed, sacrifice it" is the keyword's own trigger (CR 702.63a). It is put
        // on the stack as the last counter is removed, so the card's own exile trigger sits above it and can exile
        // the enchantment (and earn the extra turn) before it is sacrificed.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                RemoveCountersEffect.fromSelf(CounterType.TIME, 1));

        // Whenever one or more time counters are removed from this enchantment, scry 1 and gain 1
        // life. Then, if it has no time counters, exile it and take an extra turn.
        addTrigger(Triggers.timeCountersRemovedFromThis(), SequenceEffect.of(
                new ScryEffect(1),
                new GainLifeEffect(1),
                new ConditionalEffect(
                        new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                        SequenceEffect.of(new ExileTargetPermanentEffect(Subjects.source()),
                                new ConditionalEffect(HappenedThisWay.exiled(), new ControllerExtraTurnEffect(1))))));
    }
}
