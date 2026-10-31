package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "BLB", collectorNumber = "91")
@CardRegistration(set = "TLE", collectorNumber = "162")
public class Diresight extends Card {

    public Diresight() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
    }
}
