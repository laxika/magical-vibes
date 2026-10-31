package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExpropriateEffect;

@CardRegistration(set = "SPG", collectorNumber = "66")
@CardRegistration(set = "SLD", collectorNumber = "2396")
public class Expropriate extends Card {

    public Expropriate() {
        addSpellEffect(new ExpropriateEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
