package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "94")
public class CommandTheStorm extends Card {

    public CommandTheStorm() {
        target(TargetFilters.creature()).addSpellEffect(new DealDamageToTargetCreatureEffect(5));
    }
}
