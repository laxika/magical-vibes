package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "C19", collectorNumber = "8")
public class KadenasSilencer extends Card {

    public KadenasSilencer() {
        addMorph("{1}{U}");
        // Megamorph: the +1/+1 counter is put on only when the megamorph cost is paid.
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new CounterOpponentsAbilitiesEffect());
    }
}
