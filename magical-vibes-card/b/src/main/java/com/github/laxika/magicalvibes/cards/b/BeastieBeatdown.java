package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "210")
public class BeastieBeatdown extends Card {

    public BeastieBeatdown() {
        target(TargetFilters.creatureYouControl()).addSpellEffect(new ConditionalEffect(
                new Delirium(),
                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));

        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(DealDamageEffect.bite());
    }
}
