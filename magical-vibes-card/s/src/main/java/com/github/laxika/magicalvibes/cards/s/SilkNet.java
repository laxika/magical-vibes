package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "112")
public class SilkNet extends Card {

    public SilkNet() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()));
    }
}
