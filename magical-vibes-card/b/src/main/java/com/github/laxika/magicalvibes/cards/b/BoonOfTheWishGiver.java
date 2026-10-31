package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IKO", collectorNumber = "43")
@CardRegistration(set = "LTC", collectorNumber = "185")
public class BoonOfTheWishGiver extends Card {

    public BoonOfTheWishGiver() {
        addSpellEffect(new DrawCardEffect(4));
        addCycling("{1}");
    }
}
