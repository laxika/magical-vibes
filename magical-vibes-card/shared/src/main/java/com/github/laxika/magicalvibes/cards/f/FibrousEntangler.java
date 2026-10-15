package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

public class FibrousEntangler extends Card {

    public FibrousEntangler() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
        addEffect(EffectSlot.STATIC, new MustBeBlockedIfAbleEffect());
    }
}
