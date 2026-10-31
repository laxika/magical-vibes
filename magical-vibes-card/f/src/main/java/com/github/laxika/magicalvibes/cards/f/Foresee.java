package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "54")
@CardRegistration(set = "FUT", collectorNumber = "36")
@CardRegistration(set = "TSR", collectorNumber = "69")
public class Foresee extends Card {

    public Foresee() {
        addSpellEffect(new ScryEffect(4));
        addSpellEffect(new DrawCardEffect(2));
    }
}
