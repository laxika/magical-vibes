package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "219")
@CardRegistration(set = "WOE", collectorNumber = "246")
public class Gingerbrute extends Card {

    public Gingerbrute() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasKeywordPredicate(Keyword.HASTE),
                        "creatures with haste",
                        true)),
                "{1}: This creature can't be blocked this turn except by creatures with haste."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this creature: You gain 3 life."
        ));
    }
}
