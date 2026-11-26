package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "C15", collectorNumber = "28")
@CardRegistration(set = "CM2", collectorNumber = "114")
public class MeteorBlast extends Card {

    public MeteorBlast() {
        targetExactlyX(null, Integer.MAX_VALUE).addSpellEffect(DealDamageEffect.toEachTarget(new Fixed(4)));
    }
}
