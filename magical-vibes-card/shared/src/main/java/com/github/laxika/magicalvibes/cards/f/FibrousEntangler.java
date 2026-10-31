package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

public class FibrousEntangler extends Card {

    public FibrousEntangler() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
        addStaticEffect(new MustBeBlockedIfAbleEffect());
    }
}
