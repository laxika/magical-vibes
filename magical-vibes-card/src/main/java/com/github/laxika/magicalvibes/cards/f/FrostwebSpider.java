package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "CSP", collectorNumber = "109")
public class FrostwebSpider extends Card {

    public FrostwebSpider() {
        // Whenever this creature blocks a creature with flying, put a +1/+1 counter on it at end of combat.
        addEffect(EffectSlot.ON_BLOCK, new TriggeringPermanentConditionalEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                new PutCounterOnSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
