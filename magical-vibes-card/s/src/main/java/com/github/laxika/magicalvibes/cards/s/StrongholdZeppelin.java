package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "NEM", collectorNumber = "47")
public class StrongholdZeppelin extends Card {

    public StrongholdZeppelin() {
        addStaticEffect(new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING),
                "creatures with flying"
        ));
    }
}
