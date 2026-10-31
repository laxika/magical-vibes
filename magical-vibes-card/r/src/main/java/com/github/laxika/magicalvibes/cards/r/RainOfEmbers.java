package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "RAV", collectorNumber = "138")
public class RainOfEmbers extends Card {

    public RainOfEmbers() {
        addSpellEffect(new MassDamageEffect(1, true));
    }
}
