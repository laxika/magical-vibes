package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "WAR", collectorNumber = "204")
@CardRegistration(set = "SLD", collectorNumber = "1715")
@CardRegistration(set = "RVR", collectorNumber = "199")
@CardRegistration(set = "DSC", collectorNumber = "225")
@CardRegistration(set = "EOC", collectorNumber = "121")
public class MayhemDevil extends Card {

    public MayhemDevil() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_SACRIFICED, new DealDamageToAnyTargetEffect(1));
    }
}
