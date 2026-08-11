package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "KTK", collectorNumber = "18")
public class MasterOfPearls extends Card {

    public MasterOfPearls() {
        addMorph("{3}{W}{W}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new BoostAllOwnCreaturesEffect(2, 2));
    }
}
