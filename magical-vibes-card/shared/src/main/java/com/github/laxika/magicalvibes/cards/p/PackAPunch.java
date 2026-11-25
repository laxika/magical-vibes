package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Pack a Punch, the prepare spell of Kirol, History Buff // Pack a Punch (SOS 198). */
public class PackAPunch extends Card {

    public PackAPunch() {
        addSpellEffect(new MillEffect(1, Subjects.you()));

        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
    }
}
