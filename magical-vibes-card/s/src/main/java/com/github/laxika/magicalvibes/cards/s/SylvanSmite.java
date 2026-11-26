package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControllerWasNotStartingPlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YBRO", collectorNumber = "15")
public class SylvanSmite extends Card {

    public SylvanSmite() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new ConditionalEffect(
                        new ControllerWasNotStartingPlayer(),
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE)))
                .addSpellEffect(DealDamageEffect.bite());
        target(TargetFilters.creatureAnOpponentControls());
    }
}
