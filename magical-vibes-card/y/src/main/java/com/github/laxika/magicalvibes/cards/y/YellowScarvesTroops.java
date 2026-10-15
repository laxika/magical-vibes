package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "PTK", collectorNumber = "127")
public class YellowScarvesTroops extends Card {

    public YellowScarvesTroops() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
