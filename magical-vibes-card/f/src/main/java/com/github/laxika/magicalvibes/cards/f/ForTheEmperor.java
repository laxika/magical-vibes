package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "40K", collectorNumber = "12")
public class ForTheEmperor extends Card {

    public ForTheEmperor() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 2));
        addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
        addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES));
    }
}
