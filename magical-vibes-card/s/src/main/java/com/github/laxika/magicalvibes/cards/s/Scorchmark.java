package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "113")
public class Scorchmark extends Card {

    public Scorchmark() {
        // Scorchmark deals 2 damage to target creature. If that creature would die this turn,
        // exile it instead.
        target(TargetFilters.creature())
                .addSpellEffect(new MarkTargetCreatureExileInsteadOfDieThisTurnEffect())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(2));
    }
}
