package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "OGW", collectorNumber = "90")
public class TarSnare extends Card {

    public TarSnare() {
        addSpellEffect(new BoostTargetCreatureEffect(-3, -2));
    }
}
