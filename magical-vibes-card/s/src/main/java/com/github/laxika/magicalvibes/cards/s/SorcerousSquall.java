package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndMayCastSpellFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "LTC", collectorNumber = "504")
@CardRegistration(set = "LTC", collectorNumber = "548")
public class SorcerousSquall extends Card {

    public SorcerousSquall() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addSpellEffect(new MillTargetPlayerAndMayCastSpellFromGraveyardEffect(9));
    }
}
