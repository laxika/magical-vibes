package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "LGN", collectorNumber = "11")
public class DefenderOfTheOrder extends Card {

    public DefenderOfTheOrder() {
        addMorph("{W}{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new BoostAllOwnCreaturesEffect(0, 2));
    }
}
