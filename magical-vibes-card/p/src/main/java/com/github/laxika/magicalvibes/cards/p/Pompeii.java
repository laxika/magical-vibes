package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "WHO", collectorNumber = "595")
public class Pompeii extends Card {

    public Pompeii() {
        PutCountersOnSelfEffect eruptionCounter = new PutCountersOnSelfEffect(CounterType.ERUPTION);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, eruptionCounter);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, eruptionCounter);

        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                new ConditionalEffect(new NotCondition(new EventValueAtLeast(1)),
                        SequenceEffect.of(new ScryEffect(2), eruptionCounter)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                new MassDamageEffect(new CountersOnSource(CounterType.ERUPTION), true),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER),
                new PlaneswalkEffect()));
    }
}
