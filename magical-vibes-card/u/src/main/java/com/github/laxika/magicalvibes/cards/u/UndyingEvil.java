package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "77")
public class UndyingEvil extends Card {

    public UndyingEvil() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.UNDYING, Subjects.target()));
    }
}
