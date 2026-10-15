package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsAbilitiesEffect;

@CardRegistration(set = "C19", collectorNumber = "8")
public class KadenasSilencer extends Card {

    public KadenasSilencer() {
        addMorph("{1}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new CounterOpponentsAbilitiesEffect());
    }
}
