package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "CHK", collectorNumber = "198")
public class YamabushisFlame extends Card {

    public YamabushisFlame() {
        // 3 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Fixed(3), false, true));
    }
}
