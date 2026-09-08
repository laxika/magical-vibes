package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "207")
public class MarshalOfTheLost extends Card {

    public MarshalOfTheLost() {
        // Whenever you attack, target creature gets +X/+X until end of turn, where X is the number
        // of attacking creatures. The count is evaluated as the ability resolves.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new BoostTargetCreatureEffect(
                        new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER),
                        new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER)));
    }
}
