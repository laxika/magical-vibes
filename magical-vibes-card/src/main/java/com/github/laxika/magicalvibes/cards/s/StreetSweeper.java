package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "234")
public class StreetSweeper extends Card {

    public StreetSweeper() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_ATTACK,
                new DestroyAttachmentsOnTargetCreatureEffect(true, false, TargetCategory.LAND));
    }
}
