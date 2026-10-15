package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "11")
public class KnightOfValor extends Card {

    public KnightOfValor() {
        // Flanking is auto-loaded from Scryfall and handled by the engine.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new BoostAllCreaturesEffect(-1, -1, new PermanentAllOfPredicate(List.of(
                        new PermanentBlockingSourcePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLANKING)))))),
                "{1}{W}: Each creature without flanking blocking this creature gets -1/-1 until end of turn. Activate only once each turn.",
                1));
    }
}
