package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "25")
public class HiredTorturer extends Card {

    public HiredTorturer() {
        // {3}{B}, {T}: Target opponent loses 2 life, then reveals a card at random from their hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{3}{B}",
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                        new RevealRandomCardFromTargetPlayerHandEffect()
                ),
                "{3}{B}, {T}: Target opponent loses 2 life, then reveals a card at random from their hand.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
