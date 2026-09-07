package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "RAV", collectorNumber = "34")
public class VeteranArmorer extends Card {

    public VeteranArmorer() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES));
    }
}
