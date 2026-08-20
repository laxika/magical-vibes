package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "88")
public class TenuredInkcaster extends Card {

    public TenuredInkcaster() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1))));
    }
}
