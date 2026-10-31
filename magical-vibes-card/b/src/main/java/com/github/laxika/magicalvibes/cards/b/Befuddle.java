package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DOM", collectorNumber = "45")
@CardRegistration(set = "M20", collectorNumber = "47")
public class Befuddle extends Card {

    public Befuddle() {
        addSpellEffect(new BoostTargetCreatureEffect(-4, 0));
        addSpellEffect(new DrawCardEffect());
    }
}
