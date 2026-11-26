package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "34")
public class DazzlingLights extends Card {

    public DazzlingLights() {
        addSpellEffect(BoostEffect.target(-3, 0));
        addSpellEffect(new SurveilEffect(2));
    }
}
