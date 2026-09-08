package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "4")
public class ArtfulManeuver extends Card {

    public ArtfulManeuver() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
