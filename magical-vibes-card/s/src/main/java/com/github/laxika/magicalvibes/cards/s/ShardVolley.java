package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MOR", collectorNumber = "103")
public class ShardVolley extends Card {

    public ShardVolley() {
        addCastingCost(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"));
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
    }
}
