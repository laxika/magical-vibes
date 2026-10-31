package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "117")
public class FerventStrike extends Card {

    public FerventStrike() {
        addSpellEffect(new BoostTargetCreatureEffect(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
        addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
