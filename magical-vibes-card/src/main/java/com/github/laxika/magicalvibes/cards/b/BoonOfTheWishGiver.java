package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IKO", collectorNumber = "43")
public class BoonOfTheWishGiver extends Card {

    public BoonOfTheWishGiver() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(4));
        addCycling("{1}");
    }
}
