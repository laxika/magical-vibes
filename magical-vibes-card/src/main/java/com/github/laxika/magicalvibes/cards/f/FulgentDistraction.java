package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;

@CardRegistration(set = "SOM", collectorNumber = "7")
public class FulgentDistraction extends Card {

    public FulgentDistraction() {
        setMinTargets(2);
        setMaxTargets(2);
        addEffect(EffectSlot.SPELL, new TapTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new UnattachEquipmentFromTargetPermanentsEffect());
    }
}
