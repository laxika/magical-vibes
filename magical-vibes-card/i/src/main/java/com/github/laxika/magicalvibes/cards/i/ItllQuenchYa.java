package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "TLA", collectorNumber = "58")
public class ItllQuenchYa extends Card {

    public ItllQuenchYa() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(2));
    }
}
