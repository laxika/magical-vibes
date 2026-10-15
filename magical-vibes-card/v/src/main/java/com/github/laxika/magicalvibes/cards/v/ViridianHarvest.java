package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NPH", collectorNumber = "125")
public class ViridianHarvest extends Card {

    public ViridianHarvest() {
        // When enchanted artifact is put into a graveyard, you gain 6 life.
        target(TargetFilters.artifact()).addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new GainLifeEffect(6));
    }
}
