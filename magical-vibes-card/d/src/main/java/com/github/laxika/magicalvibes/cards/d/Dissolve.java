package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "47")
@CardRegistration(set = "IMA", collectorNumber = "51")
public class Dissolve extends Card {

    public Dissolve() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ScryEffect(1));
    }
}
