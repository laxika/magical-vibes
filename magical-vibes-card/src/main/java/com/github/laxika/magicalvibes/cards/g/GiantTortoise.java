package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "4ED", collectorNumber = "76")
public class GiantTortoise extends Card {

    public GiantTortoise() {
        // This creature gets +0/+3 as long as it's untapped.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                0, 3, GrantScope.SELF, new PermanentNotPredicate(new PermanentIsTappedPredicate())));
    }
}
