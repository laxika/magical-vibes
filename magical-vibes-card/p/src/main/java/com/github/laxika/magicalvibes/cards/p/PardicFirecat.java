package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CountAsNamedCardForSpellEffect;

@CardRegistration(set = "ODY", collectorNumber = "211")
public class PardicFirecat extends Card {

    public PardicFirecat() {
        addStaticEffect(new CountAsNamedCardForSpellEffect("Flame Burst", "Flame Burst"));
    }
}
