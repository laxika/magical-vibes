package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
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
                List.of(new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)),
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
                        new DestroyEachTargetPermanentEffect(),
                        new MayEffect(returnCreatureFromAnyGraveyard(),
                                "Put a creature card from a graveyard onto the battlefield?"),
                        new MayEffect(returnCreatureFromAnyGraveyard(),
                                "Put a creature card from a graveyard onto the battlefield?")
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

    private static ReturnCardFromGraveyardEffect returnCreatureFromAnyGraveyard() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .build();
    }
}
