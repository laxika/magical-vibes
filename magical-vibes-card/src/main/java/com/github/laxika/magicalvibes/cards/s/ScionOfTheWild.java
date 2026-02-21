package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;

@CardRegistration(set = "10E", collectorNumber = "295")
public class ScionOfTheWild extends Card {

    public ScionOfTheWild() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledCreatureCountEffect());
    }
}
