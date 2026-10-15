package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeDestroyedByLethalDamageUnlessSingleSourceEffect;

@CardRegistration(set = "VIS", collectorNumber = "89")
public class OgreEnforcer extends Card {

    public OgreEnforcer() {
        // This creature can't be destroyed by lethal damage unless lethal damage dealt by a
        // single source is marked on it.
        addEffect(EffectSlot.STATIC, new CantBeDestroyedByLethalDamageUnlessSingleSourceEffect());
    }
}
