package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "118")
@CardRegistration(set = "CMD", collectorNumber = "116")
@CardRegistration(set = "CMA", collectorNumber = "78")
public class CleansingBeam extends Card {

    public CleansingBeam() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetAndSharingCreaturesEffect(2));
    }
}
