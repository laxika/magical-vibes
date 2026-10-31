package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExplosionOfRichesEffect;

@CardRegistration(set = "SCD", collectorNumber = "140")
public class ExplosionOfRiches extends Card {

    public ExplosionOfRiches() {
        addSpellEffect(new ExplosionOfRichesEffect());
    }
}
