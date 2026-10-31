package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;

@CardRegistration(set = "6ED", collectorNumber = "47")
@CardRegistration(set = "WTH", collectorNumber = "28")
public class Tariff extends Card {

    public Tariff() {
        addSpellEffect(new EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect());
    }
}
