package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "10E", collectorNumber = "244")
public class UncontrollableAnger extends Card {

    public UncontrollableAnger() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new BoostEnchantedCreatureEffect(2, 2));
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
