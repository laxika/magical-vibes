package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromAllOtherManaValuesEffect;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "218")
public class HaktosTheUnscarred extends Card {

    public HaktosTheUnscarred() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseRandomNumberOnEnterEffect(2, 4));
        addEffect(EffectSlot.STATIC, new ProtectionFromAllOtherManaValuesEffect(Set.of(2, 3, 4)));
    }
}
