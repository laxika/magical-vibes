package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOS", collectorNumber = "65")
@CardRegistration(set = "WOE", collectorNumber = "65")
@CardRegistration(set = "FDN", collectorNumber = "513")
public class QuickStudy extends Card {

    public QuickStudy() {
        addSpellEffect(new DrawCardEffect(2));
    }
}
