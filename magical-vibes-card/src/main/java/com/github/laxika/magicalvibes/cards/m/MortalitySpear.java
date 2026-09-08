package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "207")
public class MortalitySpear extends Card {

    public MortalitySpear() {
        // This spell costs {2} less to cast if you gained life this turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GainedLifeThisTurn(), new ReduceOwnCastCostEffect(new Fixed(2))));

        // Destroy target nonland permanent.
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
