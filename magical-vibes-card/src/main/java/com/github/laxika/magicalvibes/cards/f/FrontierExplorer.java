package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromOutsideGameThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "6")
public class FrontierExplorer extends Card {

    public FrontierExplorer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new AllowPlayCardsFromOutsideGameThisTurnEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardSubtypePredicate(CardSubtype.PLAINS))))),
                "{3}, {T}: Until end of turn, you may play one basic Plains card from outside the game."
        ));
    }
}
