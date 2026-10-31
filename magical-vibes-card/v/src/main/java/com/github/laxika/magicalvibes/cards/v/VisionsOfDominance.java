package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommandersInPlay;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnFlashbackCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIC", collectorNumber = "37")
@CardRegistration(set = "MIC", collectorNumber = "75")
public class VisionsOfDominance extends Card {

    public VisionsOfDominance() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addSpellEffect(new DoublePlusOneCountersOnTargetCreatureEffect());
        addStaticEffect(new ReduceOwnFlashbackCostEffect(new GreatestManaValueAmongOwnedCommandersInPlay()));
        addCastingOption(new FlashbackCast("{8}{G}{G}"));
    }
}
