package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "104")
@CardRegistration(set = "INR", collectorNumber = "117")
public class HeartlessSummoning extends Card {

    public HeartlessSummoning() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), new Fixed(2)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OWN_CREATURES));
    }
}
