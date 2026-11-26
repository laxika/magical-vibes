package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "116")
public class SuddenBreakthrough extends Card {

    public SuddenBreakthrough() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
