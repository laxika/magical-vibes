package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INR", collectorNumber = "49")
public class VanquishTheHorde extends Card {

    public VanquishTheHorde() {
        // This spell costs {1} less to cast for each creature on the battlefield.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
        // Destroy all creatures.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
