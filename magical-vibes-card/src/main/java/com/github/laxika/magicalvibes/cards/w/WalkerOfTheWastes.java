package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "OGW", collectorNumber = "10")
public class WalkerOfTheWastes extends Card {

    public WalkerOfTheWastes() {
        PermanentCount wastes = new PermanentCount(
                new PermanentNamedPredicate("Wastes"), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(wastes, wastes, GrantScope.SELF));
    }
}
