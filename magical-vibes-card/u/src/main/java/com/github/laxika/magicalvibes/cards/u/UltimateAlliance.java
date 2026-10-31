package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "611")
public class UltimateAlliance extends Card {

    public UltimateAlliance() {
        addSpellEffect(new DealDamageToTargetCreatureEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
        target(TargetFilters.creature());
    }
}
