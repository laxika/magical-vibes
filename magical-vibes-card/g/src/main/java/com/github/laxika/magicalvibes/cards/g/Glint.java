package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "55")
@CardRegistration(set = "ANB", collectorNumber = "28")
public class Glint extends Card {

    public Glint() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(BoostEffect.target(0, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.target()));
    }
}
