package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "DGM", collectorNumber = "24")
public class FatalFumes extends Card {

    public FatalFumes() {
        addSpellEffect(BoostEffect.target(-4, -2));
    }
}
