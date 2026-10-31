package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

/** Back face of {@link com.github.laxika.magicalvibes.cards.c.ChildOfThePack}. */
public class SavagePackmate extends Card {

    public SavagePackmate() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
    }
}
