package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "37")
public class Silverstrike extends Card {

    public Silverstrike() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(3));
    }
}
