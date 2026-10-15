package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "32")
public class Disarm extends Card {

    public Disarm() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new UnattachEquipmentFromTargetPermanentsEffect());
    }
}
