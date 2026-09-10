package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesMustAttackIfSourceAttacksEffect;

@CardRegistration(set = "ULG", collectorNumber = "93")
public class ViashinoBey extends Card {

    public ViashinoBey() {
        addEffect(EffectSlot.STATIC, new OtherCreaturesMustAttackIfSourceAttacksEffect());
    }
}
