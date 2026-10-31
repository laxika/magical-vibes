package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M20", collectorNumber = "155")
@CardRegistration(set = "SOI", collectorNumber = "176")
@CardRegistration(set = "ANB", collectorNumber = "83")
public class ReduceToAshes extends Card {

    public ReduceToAshes() {
        // Reduce to Ashes deals 5 damage to target creature. If that creature would die this
        // turn, exile it instead.
        target(TargetFilters.creature())
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
