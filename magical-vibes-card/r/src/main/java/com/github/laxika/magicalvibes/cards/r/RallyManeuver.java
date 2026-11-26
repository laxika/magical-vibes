package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "36")
@CardRegistration(set = "HBG", collectorNumber = "100")
public class RallyManeuver extends Card {

    public RallyManeuver() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));

        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(BoostEffect.target(0, 2))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()));
    }
}
