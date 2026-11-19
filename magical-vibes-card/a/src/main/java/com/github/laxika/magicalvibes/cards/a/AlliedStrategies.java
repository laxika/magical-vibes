package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PLS", collectorNumber = "20")
@CardRegistration(set = "DDE", collectorNumber = "63")
public class AlliedStrategies extends Card {

    public AlliedStrategies() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new DrawCardEffect(new BasicLandTypesAmongControlledLands(CountScope.TARGET_PLAYER), Subjects.targetPlayer()));
    }
}
