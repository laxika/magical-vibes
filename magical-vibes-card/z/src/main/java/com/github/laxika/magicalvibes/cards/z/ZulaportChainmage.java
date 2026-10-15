package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "93")
public class ZulaportChainmage extends Card {

    public ZulaportChainmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ALLY), true, false),
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)
                ),
                "{T}, Tap an untapped Ally you control: Target opponent loses 2 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")
        ));
    }
}
