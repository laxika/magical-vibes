package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "INV", collectorNumber = "146")
public class HalamDjinn extends Card {

    public HalamDjinn() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ColorMostCommonAmongAllPermanents(CardColor.RED),
                new StaticBoostEffect(-2, -2, GrantScope.SELF)));
    }
}
