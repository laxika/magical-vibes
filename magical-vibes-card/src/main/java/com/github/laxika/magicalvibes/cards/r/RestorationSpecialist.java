package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "21")
public class RestorationSpecialist extends Card {

    public RestorationSpecialist() {
        CardAnyOfPredicate artifactOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.ENCHANTMENT)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(),
                        ReturnTargetCardsFromGraveyardToHandEffect.upToOnePerCardType(
                                artifactOrEnchantment, Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT))),
                "{W}, Sacrifice this creature: Return up to one target artifact card and up to one target enchantment card from your graveyard to your hand.",
                List.of(new GraveyardCardPredicateTargetFilter(
                        artifactOrEnchantment, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                0,
                2));
    }
}
