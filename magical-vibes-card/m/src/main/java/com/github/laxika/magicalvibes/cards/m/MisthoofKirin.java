package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "DTK", collectorNumber = "25")
public class MisthoofKirin extends Card {

    public MisthoofKirin() {
        addMorph("{1}{W}");
        // Megamorph: the +1/+1 counter is put on only when the megamorph cost is paid.
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
    }
}
