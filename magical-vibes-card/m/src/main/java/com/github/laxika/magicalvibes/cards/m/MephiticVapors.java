package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "76")
@CardRegistration(set = "RVR", collectorNumber = "85")
public class MephiticVapors extends Card {

    public MephiticVapors() {
        addSpellEffect(BoostEffect.allCreatures(-1, -1));
        addSpellEffect(new SurveilEffect(2));
    }
}
