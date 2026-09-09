package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;

@CardRegistration(set = "SCG", collectorNumber = "48")
public class RiptideSurvivor extends Card {

    public RiptideSurvivor() {
        addMorph("{1}{U}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DiscardAndDrawCardEffect(2, 3));
    }
}
