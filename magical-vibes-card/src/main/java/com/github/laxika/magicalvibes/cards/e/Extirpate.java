package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;

@CardRegistration(set = "PLC", collectorNumber = "71")
public class Extirpate extends Card {

    public Extirpate() {
        addEffect(EffectSlot.SPELL, new ExileTargetGraveyardCardAndSameNameFromZonesEffect());
    }
}
