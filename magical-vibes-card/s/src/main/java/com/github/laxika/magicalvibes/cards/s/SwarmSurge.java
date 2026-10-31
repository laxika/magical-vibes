package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;

@CardRegistration(set = "BFZ", collectorNumber = "100")
public class SwarmSurge extends Card {

    public SwarmSurge() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 0));
        addSpellEffect(new GrantKeywordEffect(
                Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES, new PermanentIsColorlessPredicate()));
    }
}
