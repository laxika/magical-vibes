package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "CHK", collectorNumber = "108")
public class DanceOfShadows extends Card {

    public DanceOfShadows() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 0));
        addSpellEffect(new GrantKeywordEffect(Keyword.FEAR, GrantScope.OWN_CREATURES));
    }
}
