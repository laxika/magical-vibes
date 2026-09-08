package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "203")
public class LivingTwister extends Card {

    public LivingTwister() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{1}{R}, Discard a land card: Living Twister deals 2 damage to any target."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentIsTappedPredicate()
                        )),
                        "land"
                )),
                "{G}: Return a tapped land you control to its owner's hand."
        ));
    }
}
