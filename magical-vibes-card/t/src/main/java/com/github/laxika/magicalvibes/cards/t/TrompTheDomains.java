package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TSP", collectorNumber = "230")
@CardRegistration(set = "MMA", collectorNumber = "171")
@CardRegistration(set = "TSR", collectorNumber = "242")
public class TrompTheDomains extends Card {

    public TrompTheDomains() {
        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
        addSpellEffect(new BoostAllOwnCreaturesEffect(domain, domain));
    }
}
