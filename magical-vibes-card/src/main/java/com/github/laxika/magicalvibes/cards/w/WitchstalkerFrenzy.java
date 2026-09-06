package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "159")
public class WitchstalkerFrenzy extends Card {

    public WitchstalkerFrenzy() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new CreaturesAttackedThisTurn(CountScope.ANY_PLAYER)));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
