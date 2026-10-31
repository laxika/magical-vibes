package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "79")
public class Oxidize extends Card {

    public Oxidize() {
        target(TargetFilters.artifact()).addSpellEffect(new DestroyTargetPermanentEffect(true));
    }
}
