package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "172")
public class AcceptableLosses extends Card {

    public AcceptableLosses() {
        addCastingCost(new DiscardRandomCardCost());
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
