package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "136")
public class MagmaRift extends Card {

    public MagmaRift() {
        addCastingCost(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"));
        addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
