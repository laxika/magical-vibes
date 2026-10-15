package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;


@CardRegistration(set = "ARB", collectorNumber = "43")
public class MonstrousCarabid extends Card {

    public MonstrousCarabid() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Cycling {B/R} ({B/R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{B/R}");
    }
}
