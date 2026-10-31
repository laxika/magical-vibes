package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SNC", collectorNumber = "158")
public class TakeToTheStreets extends Card {

    public TakeToTheStreets() {
        var citizen = new PermanentHasSubtypePredicate(CardSubtype.CITIZEN);
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 2));
        addSpellEffect(new BoostAllOwnCreaturesEffect(1, 1, citizen));
        addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES, citizen));
    }
}
