package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.CounterType;

@CardRegistration(set = "DTK", collectorNumber = "34")
public class SandstormCharger extends Card {

    public SandstormCharger() {
        addMorph("{4}{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
    }
}
