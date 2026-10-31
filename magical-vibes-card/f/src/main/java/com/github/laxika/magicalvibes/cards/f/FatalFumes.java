package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "DGM", collectorNumber = "24")
public class FatalFumes extends Card {

    public FatalFumes() {
        addSpellEffect(new BoostTargetCreatureEffect(-4, -2));
    }
}
