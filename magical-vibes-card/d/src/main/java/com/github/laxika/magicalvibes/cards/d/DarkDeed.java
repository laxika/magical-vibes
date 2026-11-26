package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MSH", collectorNumber = "93")
public class DarkDeed extends Card {

    public DarkDeed() {
        addSpellEffect(BoostEffect.target(-4, -4));
    }
}
