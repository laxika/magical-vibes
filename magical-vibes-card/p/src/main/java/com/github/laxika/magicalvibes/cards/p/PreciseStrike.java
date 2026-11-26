package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "AER", collectorNumber = "92")
@CardRegistration(set = "KLR", collectorNumber = "137")
public class PreciseStrike extends Card {

    public PreciseStrike() {
        addSpellEffect(BoostEffect.target(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
    }
}
