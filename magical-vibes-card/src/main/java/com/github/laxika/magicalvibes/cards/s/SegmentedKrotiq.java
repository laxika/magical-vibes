package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "DTK", collectorNumber = "202")
public class SegmentedKrotiq extends Card {

    public SegmentedKrotiq() {
        addMorph("{6}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
    }
}
