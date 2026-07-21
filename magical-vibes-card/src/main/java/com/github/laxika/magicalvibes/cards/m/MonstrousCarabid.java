package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "43")
public class MonstrousCarabid extends Card {

    public MonstrousCarabid() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Cycling {B/R} ({B/R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{B/R}",
                List.of(new DrawCardEffect(1)),
                "Cycling {B/R} ({B/R}, Discard this card: Draw a card.)"));
    }
}
