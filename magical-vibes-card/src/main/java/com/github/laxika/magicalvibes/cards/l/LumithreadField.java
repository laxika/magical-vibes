package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "FUT", collectorNumber = "25")
public class LumithreadField extends Card {

    public LumithreadField() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES));
        addMorph("{1}{W}");
    }
}
