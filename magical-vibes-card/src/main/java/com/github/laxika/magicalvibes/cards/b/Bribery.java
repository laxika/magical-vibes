package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardToBattlefieldUnderControlEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "8ED", collectorNumber = "64")
public class Bribery extends Card {

    public Bribery() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addEffect(EffectSlot.SPELL,
                        new SearchTargetLibraryForCardToBattlefieldUnderControlEffect(
                                new CardTypePredicate(CardType.CREATURE)));
    }
}
