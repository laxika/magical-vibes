package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "207")
public class TymaretTheMurderKing extends Card {

    public TymaretTheMurderKing() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{1}{R}, Sacrifice another creature: Tymaret, the Murder King deals 2 damage to target player or planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                )
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{1}{B}: Return Tymaret, the Murder King from your graveyard to your hand."
        ));
    }
}
