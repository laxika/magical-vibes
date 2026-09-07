package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "233")
@CardRegistration(set = "KHM", collectorNumber = "194")
@CardRegistration(set = "TDM", collectorNumber = "159")
@CardRegistration(set = "OTJ", collectorNumber = "181")
public class SnakeskinVeil extends Card {

    public SnakeskinVeil() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.TARGET));
    }
}
