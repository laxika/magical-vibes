package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M13", collectorNumber = "34")
@CardRegistration(set = "M14", collectorNumber = "33")
@CardRegistration(set = "M20", collectorNumber = "311")
public class ShowOfValor extends Card {

    public ShowOfValor() {
        addSpellEffect(BoostEffect.target(2, 4));
    }
}
