package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "V15", collectorNumber = "15")
@CardRegistration(set = "2X2", collectorNumber = "281")
@CardRegistration(set = "CMD", collectorNumber = "229")
@CardRegistration(set = "CMA", collectorNumber = "194")
public class TarielReckonerOfSouls extends Card {

    public TarielReckonerOfSouls() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        ChooseCardEffect.atRandom(
                                Subjects.cardsInGraveyards(Subjects.targetPlayer(),
                                        new CardTypePredicate(CardType.CREATURE)),
                                "a creature card"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .underControlOf(Subjects.you())),
                "{T}: Choose a creature card at random from target opponent's graveyard. Put that card onto the battlefield under your control.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
