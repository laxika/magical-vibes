package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "35")
public class PunishTheEnemy extends Card {

    public PunishTheEnemy() {
        // Punish the Enemy deals 3 damage to target player or planeswalker and 3 damage to
        // target creature. Two independent targets; the same amount hits each, so the flat
        // targetIds list of DealDamageToEachTargetEffect models it (Injury's shape).
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "First target must be a player or planeswalker"
        ));
        target(TargetFilters.creature());

        addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(3)));
    }
}
