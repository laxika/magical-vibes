package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RTR", collectorNumber = "26")
@CardRegistration(set = "DDN", collectorNumber = "62")
public class SwiftJustice extends Card {

    public SwiftJustice() {
        addSpellEffect(BoostEffect.target(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
        addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()));
    }
}
