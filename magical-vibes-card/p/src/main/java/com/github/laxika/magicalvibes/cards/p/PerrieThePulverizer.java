package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DistinctCounterKindsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "5")
@CardRegistration(set = "NCC", collectorNumber = "107")
@CardRegistration(set = "NCC", collectorNumber = "190")
public class PerrieThePulverizer extends Card {

    public PerrieThePulverizer() {
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.SHIELD));

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.attacks(), new BoostTargetCreatureEffect(
                        new DistinctCounterKindsAmongControlledPermanents(),
                        new DistinctCounterKindsAmongControlledPermanents()))
                .addTrigger(Triggers.attacks(), new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
