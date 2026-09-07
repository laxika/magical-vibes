package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "DTK", collectorNumber = "5")
public class AvenSunstriker extends Card {

    public AvenSunstriker() {
        addMorph("{4}{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
    }
}
