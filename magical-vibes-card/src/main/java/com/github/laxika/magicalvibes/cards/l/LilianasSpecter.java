package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;

@CardRegistration(set = "M11", collectorNumber = "104")
public class LilianasSpecter extends Card {

    public LilianasSpecter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachOpponentDiscardsEffect(1));
    }
}
