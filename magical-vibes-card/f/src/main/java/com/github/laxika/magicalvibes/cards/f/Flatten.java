package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "DTK", collectorNumber = "100")
public class Flatten extends Card {

    public Flatten() {
        addSpellEffect(new BoostTargetCreatureEffect(-4, -4));
    }
}
