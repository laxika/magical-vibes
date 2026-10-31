package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "227")
@CardRegistration(set = "MIR", collectorNumber = "214")
public class FallowEarth extends Card {

    public FallowEarth() {
        target(TargetFilters.land())
                .addSpellEffect(new PutTargetOnTopOfLibraryEffect());
    }
}
