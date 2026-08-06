package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "104")
@CardRegistration(set = "INR", collectorNumber = "117")
@CardRegistration(set = "INR", collectorNumber = "309")
@CardRegistration(set = "INR", collectorNumber = "383")
public class HeartlessSummoning extends Card {

    public HeartlessSummoning() {
        // Creature spells you cast cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(2), CostModificationScope.SELF));
        // Creatures you control get -1/-1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OWN_CREATURES));
    }
}
