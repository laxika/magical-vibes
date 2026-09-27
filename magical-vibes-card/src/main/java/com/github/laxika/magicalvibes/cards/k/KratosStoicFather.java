package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "2213")
public class KratosStoicFather extends Card {

    public KratosStoicFather() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.GOD)),
                new ExperienceCountersEffect(1)));

        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.GOD),
                new ExperienceCountersEffect(1)));
        addEffect(EffectSlot.ON_DEATH, new ExperienceCountersEffect(1));

        target(TargetFilters.creature()).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new ControllerExperienceCounters()));
    }
}
