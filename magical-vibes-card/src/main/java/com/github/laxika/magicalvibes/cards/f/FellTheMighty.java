package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerGreaterThanTargetEffect;

@CardRegistration(set = "SLD", collectorNumber = "1778")
public class FellTheMighty extends Card {

    public FellTheMighty() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesWithPowerGreaterThanTargetEffect());
    }
}
