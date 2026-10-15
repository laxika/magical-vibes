package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "ECC", collectorNumber = "11")
@CardRegistration(set = "ECC", collectorNumber = "31")
public class OftNabbedGoat extends Card {

    public OftNabbedGoat() {
        CounterType counterType = CounterType.MINUS_ONE_MINUS_ONE;

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DrawCardEffect(),
                        new ControllerGainsControlOfSourceCreatureEffect(),
                        new PutCountersOnSelfEffect(counterType)
                ),
                "{1}: Draw a card. Gain control of this creature and put a -1/-1 counter on it. "
                        + "Only your opponents may activate this ability and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivatableByAnyPlayer().withActivatableOnlyByOpponents());

        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new SourceCounterThreshold(1, counterType),
                SequenceEffect.of(
                        new DrawCardForOwnerEffect(new CountersOnSource(counterType)),
                        new LoseLifeEffect(new CountersOnSource(counterType), LoseLifeRecipient.EACH_OTHER_PLAYER)
                )));
    }
}
