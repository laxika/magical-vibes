package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "4")
@CardRegistration(set = "PIO", collectorNumber = "7")
public class ArtfulManeuver extends Card {

    public ArtfulManeuver() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));
    }
}
