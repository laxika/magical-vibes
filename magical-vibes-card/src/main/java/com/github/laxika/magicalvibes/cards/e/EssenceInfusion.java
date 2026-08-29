package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "69")
public class EssenceInfusion extends Card {

    public EssenceInfusion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
