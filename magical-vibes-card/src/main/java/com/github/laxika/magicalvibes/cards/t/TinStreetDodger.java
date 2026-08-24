package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "120")
public class TinStreetDodger extends Card {

    public TinStreetDodger() {
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                        "creatures with defender",
                        true)),
                "{R}: This creature can't be blocked this turn except by creatures with defender."));
    }
}
