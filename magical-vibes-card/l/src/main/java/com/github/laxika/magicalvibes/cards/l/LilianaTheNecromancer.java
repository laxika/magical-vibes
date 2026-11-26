package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "291")
public class LilianaTheNecromancer extends Card {

    public LilianaTheNecromancer() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LoseLifeEffect(2, Subjects.targetPlayer())),
                "+1: Target player loses 2 life.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build()),
                "−1: Return target creature card from your graveyard to your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        DestroyTargetPermanentEffect.eachTarget(),
                        ChooseCardEffect.upTo(
                                Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.CREATURE)),
                                2, "up to two creature cards from graveyards"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .underControlOf(Subjects.you())
                ),
                "−7: Destroy up to two target creatures. Put up to two creature cards from graveyards onto the battlefield under your control.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                ),
                -7, null, null,
                List.of(), 0, 2
        ));
    }
}
