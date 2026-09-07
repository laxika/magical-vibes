package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;

@CardRegistration(set = "DTK", collectorNumber = "141")
public class IreShaman extends Card {

    public IreShaman() {
        addMorph("{R}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(1));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
