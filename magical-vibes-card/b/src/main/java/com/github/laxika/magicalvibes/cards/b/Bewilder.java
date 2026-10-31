package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "49")
@CardRegistration(set = "IMA", collectorNumber = "43")
@CardRegistration(set = "TSR", collectorNumber = "53")
public class Bewilder extends Card {

    public Bewilder() {
        // Target creature gets -3/-0 until end of turn. Draw a card.
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-3, 0));
        addSpellEffect(new DrawCardEffect(1));
    }
}
