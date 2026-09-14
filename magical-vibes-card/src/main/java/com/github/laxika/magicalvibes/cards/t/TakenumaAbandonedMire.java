package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "278")
public class TakenumaAbandonedMire extends Card {

    public TakenumaAbandonedMire() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        CardAnyOfPredicate creatureOrPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                                )),
                                CountScope.CONTROLLER
                        )),
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(creatureOrPlaneswalker)
                                .targetGraveyard(true)
                                .build()
                ),
                "Channel — {3}{B}, Discard this card: Mill three cards, then return a creature or planeswalker card "
                        + "from your graveyard to your hand. This ability costs {1} less to activate for each legendary "
                        + "creature you control.",
                new GraveyardCardPredicateTargetFilter(
                        creatureOrPlaneswalker, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
        ));
    }
}
