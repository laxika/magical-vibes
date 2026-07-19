package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "24")
public class CumberStone extends Card {

    public CumberStone() {
        // Creatures your opponents control get -1/-0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES));
    }
}
