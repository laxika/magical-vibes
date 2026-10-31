package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FDN", collectorNumber = "109")
@CardRegistration(set = "FDN", collectorNumber = "338")
@CardRegistration(set = "FDN", collectorNumber = "400")
@CardRegistration(set = "FDN", collectorNumber = "472")
public class PreposterousProportions extends Card {

    public PreposterousProportions() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(10, 10));
        addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
    }
}
