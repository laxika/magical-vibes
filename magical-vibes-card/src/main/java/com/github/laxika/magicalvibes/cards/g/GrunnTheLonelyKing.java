package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttacksAloneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DOM", collectorNumber = "165")
public class GrunnTheLonelyKing extends Card {

    public GrunnTheLonelyKing() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithPlusOnePlusOneCountersIfKickedEffect(5));
        addEffect(EffectSlot.ON_ATTACK, new AttacksAloneConditionalEffect(new DoubleSelfPowerToughnessEffect()));
    }
}
