package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKC", collectorNumber = "108")
@CardRegistration(set = "NEC", collectorNumber = "14")
@CardRegistration(set = "NEC", collectorNumber = "50")
@CardRegistration(set = "EOC", collectorNumber = "74")
@CardRegistration(set = "FDC", collectorNumber = "64")
public class KappaCannoneer extends Card {

    public KappaCannoneer() {
        // Ward {4}
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(4));
        // Whenever this creature or another artifact you control enters, put a +1/+1 counter on
        // this creature. It can't be blocked this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect()));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect())));
    }
}
