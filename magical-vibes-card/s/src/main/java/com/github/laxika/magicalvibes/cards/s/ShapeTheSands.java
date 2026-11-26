package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "205")
public class ShapeTheSands extends Card {

    public ShapeTheSands() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(0, 5))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()));
    }
}
