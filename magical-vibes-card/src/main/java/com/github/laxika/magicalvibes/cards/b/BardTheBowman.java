package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "145")
public class BardTheBowman extends Card {

    public BardTheBowman() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                        SequenceEffect.of(
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)));
    }
}
