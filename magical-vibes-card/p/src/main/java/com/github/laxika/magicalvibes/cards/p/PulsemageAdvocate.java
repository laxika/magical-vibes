package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "19")
public class PulsemageAdvocate extends Card {

    public PulsemageAdvocate() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        ReturnTargetCardsFromGraveyardToHandEffect returnToOwnersHands =
                new ReturnTargetCardsFromGraveyardToHandEffect(null, 3)
                        .withTargetGroups(0, 1, 2)
                        .fromSameGraveyard()
                        .toOwnersHands();
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .filter(creature)
                .targetGraveyard(true)
                .targetGroup(3)
                .build();

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(returnToOwnersHands, returnCreature),
                "{T}: Return three target cards from an opponent's graveyard to their hand. Return target creature card from your graveyard to the battlefield.",
                List.of(
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new GraveyardCardPredicateTargetFilter(creature, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                4,
                4));
    }
}
