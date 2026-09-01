package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MAT", collectorNumber = "41")
public class ObNixilisCaptiveKingpin extends Card {

    public ObNixilisCaptiveKingpin() {
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ExileTopCardsMayPlayUntilNextEndStepEffect(1)));
    }
}
