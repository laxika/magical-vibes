package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTopCreatureCardInLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YECL", collectorNumber = "27")
public class ThornaAndTwigtooth extends Card {

    public ThornaAndTwigtooth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ATTACK,
                SequenceEffect.of(
                        new RemoveAllCountersFromTargetCreatureEffect(),
                        new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(new EventValue()),
                        new PerpetuallyBoostTopCreatureCardInLibraryEffect(new EventValue())));
    }
}
