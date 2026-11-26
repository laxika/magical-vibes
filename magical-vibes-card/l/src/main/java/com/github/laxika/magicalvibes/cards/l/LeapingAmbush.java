package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "177")
public class LeapingAmbush extends Card {

    public LeapingAmbush() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.target()))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
