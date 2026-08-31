package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;

@CardRegistration(set = "WOE", collectorNumber = "34")
public class ATaleForTheAges extends Card {

    public ATaleForTheAges() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentIsEnchantedPredicate()));
    }
}
