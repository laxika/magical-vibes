package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "205")
public class MightSliver extends Card {

    public MightSliver() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
