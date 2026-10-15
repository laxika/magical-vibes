package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerDealtDamageThisTurnPredicate;

@CardRegistration(set = "LRW", collectorNumber = "186")
public class NeedleDrop extends Card {

    public NeedleDrop() {
        // "deals 1 damage to any target that was dealt damage this turn" — an any-target damage
        // spell whose target (creature/planeswalker or player) must have taken damage this turn.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentDealtDamageThisTurnPredicate(),
                new PlayerDealtDamageThisTurnPredicate(),
                "Target must have been dealt damage this turn"
        )).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));

        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
