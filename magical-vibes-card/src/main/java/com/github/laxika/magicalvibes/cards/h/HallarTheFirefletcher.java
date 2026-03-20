package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "196")
public class HallarTheFirefletcher extends Card {

    public HallarTheFirefletcher() {
        // Whenever you cast a spell, if that spell was kicked,
        // put a +1/+1 counter on Hallar, the Firefletcher,
        // then Hallar deals damage equal to the number of +1/+1 counters on it to each opponent.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new KickedSpellCastTriggerEffect(
                List.of(
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect()
                )
        ));
    }
}
