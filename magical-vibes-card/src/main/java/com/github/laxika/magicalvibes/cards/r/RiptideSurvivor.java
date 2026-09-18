package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SCG", collectorNumber = "48")
public class RiptideSurvivor extends Card {

    public RiptideSurvivor() {
        addMorph("{1}{U}{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new DrawCardEffect(3));
    }
}
