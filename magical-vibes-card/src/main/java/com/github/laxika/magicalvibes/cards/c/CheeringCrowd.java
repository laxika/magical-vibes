package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SPM", collectorNumber = "126")
public class CheeringCrowd extends Card {

    public CheeringCrowd() {
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new MayEffect(
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new AwardManaToActivePlayerEffect(
                                        ManaColor.COLORLESS,
                                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                        "Put a +1/+1 counter on Cheering Crowd?",
                        null,
                        MayChoicePlayer.ACTIVE_PLAYER));
    }
}
