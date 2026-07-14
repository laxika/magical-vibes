package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;

@CardRegistration(set = "6ED", collectorNumber = "47")
public class Tariff extends Card {

    public Tariff() {
        addEffect(EffectSlot.SPELL, new EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect());
    }
}
