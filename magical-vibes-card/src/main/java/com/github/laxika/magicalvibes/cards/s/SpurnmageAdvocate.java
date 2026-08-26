package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "27")
public class SpurnmageAdvocate extends Card {

    public SpurnmageAdvocate() {
        ReturnTargetCardsFromGraveyardToHandEffect returnCards =
                new ReturnTargetCardsFromGraveyardToHandEffect(null, 2)
                        .withTargetGroups(0, 1)
                        .fromSameGraveyard()
                        .toOwnersHands();

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(returnCards, new DestroyTargetPermanentEffect()),
                "{T}: Return two target cards from an opponent's graveyard to their hand. Destroy target attacking creature.",
                List.of(
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        TargetFilters.attackingCreature()),
                3,
                3));
    }
}
