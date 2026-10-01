package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "185")
public class BitterDownfall extends Card {

    public BitterDownfall() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingPermanentEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentDealtDamageThisTurnPredicate()
                )), 3));

        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new LoseLifeEffect(2), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
