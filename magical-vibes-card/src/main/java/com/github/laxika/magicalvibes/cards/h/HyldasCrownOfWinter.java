package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DuringControllerTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "247")
public class HyldasCrownOfWinter extends Card {

    public HyldasCrownOfWinter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new ReduceActivationCostEffect(new DuringControllerTurn(new Fixed(1))),
                        new TapPermanentsEffect(TapUntapScope.TARGET)
                ),
                "{1}, {T}: Tap target creature. This ability costs {1} less to activate during your turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsTappedPredicate()
                                )),
                                CountScope.OPPONENTS
                        ))
                ),
                "{3}, Sacrifice Hylda's Crown of Winter: Draw a card for each tapped creature your opponents control."
        ));
    }
}
