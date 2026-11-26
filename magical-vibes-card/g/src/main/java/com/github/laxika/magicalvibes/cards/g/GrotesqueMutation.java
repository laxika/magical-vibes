package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "115")
@CardRegistration(set = "SIR", collectorNumber = "116")
public class GrotesqueMutation extends Card {

    public GrotesqueMutation() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()));
    }
}
