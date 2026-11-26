package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DOM", collectorNumber = "45")
@CardRegistration(set = "M20", collectorNumber = "47")
public class Befuddle extends Card {

    public Befuddle() {
        addSpellEffect(BoostEffect.target(-4, 0));
        addSpellEffect(new DrawCardEffect());
    }
}
