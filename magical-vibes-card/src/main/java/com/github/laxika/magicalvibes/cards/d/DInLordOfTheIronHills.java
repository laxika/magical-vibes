package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerHasEnduringStory;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StoriedEffect;

@CardRegistration(set = "HOB", collectorNumber = "8")
public class DInLordOfTheIronHills extends Card {

    public DInLordOfTheIronHills() {
        addEffect(EffectSlot.STATIC, new StoriedEffect());
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(
                new Fixed(1), false, new ControllerHasEnduringStory()));
    }
}
