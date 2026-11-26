package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "117")
public class FerventStrike extends Card {

    public FerventStrike() {
        addSpellEffect(BoostEffect.target(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()));
    }
}
