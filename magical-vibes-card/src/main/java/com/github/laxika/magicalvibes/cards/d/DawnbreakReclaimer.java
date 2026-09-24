package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DawnbreakReclaimerEffect;

@CardRegistration(set = "SLD", collectorNumber = "1346")
public class DawnbreakReclaimer extends Card {

    public DawnbreakReclaimer() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DawnbreakReclaimerEffect());
    }
}
