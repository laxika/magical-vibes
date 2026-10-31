package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "140")
public class ImpossibleInferno extends Card {

    public ImpossibleInferno() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(6))
                .addSpellEffect(new ConditionalEffect(new Delirium(),
                        new ExileTopCardsMayPlayUntilNextTurnEffect(1)));
    }
}
