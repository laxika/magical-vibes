package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;

@CardRegistration(set = "NPH", collectorNumber = "74")
public class SurgicalExtraction extends Card {

    public SurgicalExtraction() {
        addEffect(EffectSlot.SPELL, new ExileTargetGraveyardCardAndSameNameFromZonesEffect());
    }
}
