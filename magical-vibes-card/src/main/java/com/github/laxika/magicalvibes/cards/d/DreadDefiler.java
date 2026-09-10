package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "68")
public class DreadDefiler extends Card {

    public DreadDefiler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{C}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE, false, true),
                        new LoseLifeEffect(new ImprintedCreaturePower(), LoseLifeRecipient.TARGET_PLAYER)
                ),
                "{3}{C}, Exile a creature card from your graveyard: Target opponent loses life equal to the exiled card's power.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
