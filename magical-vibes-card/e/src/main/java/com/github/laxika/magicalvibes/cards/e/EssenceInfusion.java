package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "69")
public class EssenceInfusion extends Card {

    public EssenceInfusion() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()));
    }
}
