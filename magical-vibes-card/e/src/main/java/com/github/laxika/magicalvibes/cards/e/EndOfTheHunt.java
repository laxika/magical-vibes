package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "SOS", collectorNumber = "81")
public class EndOfTheHunt extends Card {

    public EndOfTheHunt() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect());
    }
}
