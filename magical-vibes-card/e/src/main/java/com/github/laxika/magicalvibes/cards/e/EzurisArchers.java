package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "120")
@CardRegistration(set = "DDU", collectorNumber = "9")
public class EzurisArchers extends Card {

    public EzurisArchers() {
        // Reach keyword is auto-loaded from Scryfall.
        // Whenever this creature blocks a creature with flying, this creature gets +3/+0 until end of turn.
        addTrigger(Triggers.blocks().withFilter(new PermanentHasKeywordPredicate(Keyword.FLYING)), new BoostSelfEffect(3, 0));
    }
}
