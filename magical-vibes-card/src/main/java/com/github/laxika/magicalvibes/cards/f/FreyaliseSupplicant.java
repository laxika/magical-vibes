package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.EnumSet;
import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "239")
public class FreyaliseSupplicant extends Card {

    public FreyaliseSupplicant() {
        // The sacrifice cost snapshots the sacrificed creature's effective power into the
        // entry's xValue; Divided(_, 2) halves it rounded down.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentColorInPredicate(EnumSet.of(CardColor.RED, CardColor.WHITE)))),
                                "a red or white creature",
                                false,
                                true),
                        new DealDamageToAnyTargetEffect(new Divided(new XValue(), 2))
                ),
                "{T}, Sacrifice a red or white creature: This creature deals damage to any target "
                        + "equal to half the sacrificed creature's power, rounded down."
        ));
    }
}
