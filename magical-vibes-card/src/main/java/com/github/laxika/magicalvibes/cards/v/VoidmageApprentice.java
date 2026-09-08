package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "LGN", collectorNumber = "54")
@CardRegistration(set = "DD2", collectorNumber = "4")
public class VoidmageApprentice extends Card {

    public VoidmageApprentice() {
        addMorph("{2}{U}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new CounterSpellEffect());
    }
}
