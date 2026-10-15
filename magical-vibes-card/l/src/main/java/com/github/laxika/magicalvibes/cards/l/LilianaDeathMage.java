package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "328")
public class LilianaDeathMage extends Card {

    public LilianaDeathMage() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .upTo(true)
                        .build()),
                "+1: Return up to one target creature card from your graveyard to your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER),
                        new DestroyTargetPermanentEffect()
                ),
                "\u22123: Destroy target creature. Its controller loses 2 life.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new LoseLifeEffect(
                        new Scaled(new CardsInGraveyard(
                                new CardTypePredicate(CardType.CREATURE), CountScope.TARGET_PLAYER), 2),
                        LoseLifeRecipient.TARGET_PLAYER)),
                "\u22127: Target opponent loses 2 life for each creature card in their graveyard.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
