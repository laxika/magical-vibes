package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "133")
public class StrongholdOverseer extends Card {

    public StrongholdOverseer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(
                        new BoostAllCreaturesEffect(1, 0,
                                new PermanentHasKeywordPredicate(Keyword.SHADOW)),
                        new BoostAllCreaturesEffect(-1, 0,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.SHADOW)))
                ),
                "{B}{B}: Creatures with shadow get +1/+0 until end of turn and creatures without shadow get -1/-0 until end of turn."
        ));
    }
}
