package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5DN", collectorNumber = "78")
public class ScreamingFury extends Card {

    public ScreamingFury() {
        // Target creature gets +5/+0 and gains haste until end of turn.
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(5, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
    }
}
