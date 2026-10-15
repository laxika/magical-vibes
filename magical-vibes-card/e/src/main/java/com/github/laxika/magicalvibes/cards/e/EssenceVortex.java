package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect;

@CardRegistration(set = "ICE", collectorNumber = "287")
public class EssenceVortex extends Card {

    public EssenceVortex() {
        // "Destroy target creature unless its controller pays life equal to its toughness.
        //  A creature destroyed this way can't be regenerated."
        addEffect(EffectSlot.SPELL, new DestroyTargetCreatureUnlessControllerPaysToughnessLifeEffect());
    }
}
