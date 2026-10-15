package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DistinctCounterKindsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "5")
@CardRegistration(set = "NCC", collectorNumber = "107")
@CardRegistration(set = "NCC", collectorNumber = "190")
public class PerrieThePulverizer extends Card {

    public PerrieThePulverizer() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.SHIELD));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(
                        new DistinctCounterKindsAmongControlledPermanents(),
                        new DistinctCounterKindsAmongControlledPermanents()))
                .addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
