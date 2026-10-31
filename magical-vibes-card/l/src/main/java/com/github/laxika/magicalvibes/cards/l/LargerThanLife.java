package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "KLD", collectorNumber = "160")
@CardRegistration(set = "MB1", collectorNumber = "160")
public class LargerThanLife extends Card {

    public LargerThanLife() {
        addSpellEffect(new BoostTargetCreatureEffect(4, 4));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
