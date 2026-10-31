package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ISD", collectorNumber = "119")
@CardRegistration(set = "DDQ", collectorNumber = "65")
public class TributeToHunger extends Card {

    public TributeToHunger() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(false, false));
    }
}
