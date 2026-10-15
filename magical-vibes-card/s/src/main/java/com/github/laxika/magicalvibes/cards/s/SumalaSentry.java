package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MKM", collectorNumber = "233")
public class SumalaSentry extends Card {

    public SumalaSentry() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                SequenceEffect.of(
                        new PutCounterOnReferencedPermanentEffect(
                                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
