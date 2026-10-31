package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "98")
@CardRegistration(set = "VMA", collectorNumber = "89")
public class Repel extends Card {

    public Repel() {
        target(TargetFilters.creature()).addSpellEffect(new PutTargetOnTopOfLibraryEffect());
    }
}
