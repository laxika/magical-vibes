package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "DTK", collectorNumber = "109")
public class MarshHulk extends Card {

    public MarshHulk() {
        addMorph("{6}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
    }
}
