package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

public class WrathfulJailbreaker extends Card {

    public WrathfulJailbreaker() {
        addStaticEffect(new MustAttackEffect());
    }
}
