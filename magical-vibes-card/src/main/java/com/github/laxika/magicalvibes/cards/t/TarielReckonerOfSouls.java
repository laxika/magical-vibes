package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "V15", collectorNumber = "15")
public class TarielReckonerOfSouls extends Card {

    public TarielReckonerOfSouls() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutRandomCreatureFromTargetOpponentGraveyardOntoBattlefieldEffect()),
                "{T}: Choose a creature card at random from target opponent's graveyard. Put that card onto the battlefield under your control.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
