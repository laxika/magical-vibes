package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "126")
public class NullmageAdvocate extends Card {

    public NullmageAdvocate() {
        ReturnTargetCardsFromGraveyardToHandEffect returnCards =
                new ReturnTargetCardsFromGraveyardToHandEffect(null, 2)
                        .withTargetGroups(0, 1)
                        .fromSameGraveyard()
                        .toOwnersHands();

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(returnCards, new DestroyTargetPermanentEffect()),
                "{T}: Return two target cards from an opponent's graveyard to their hand. Destroy target artifact or enchantment.",
                List.of(
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate()
                                )),
                                "Target must be an artifact or enchantment")),
                3,
                3));
    }
}
