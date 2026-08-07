package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "89")
public class BetrothedOfFire extends Card {

    public BetrothedOfFire() {
        // Enchant creature
        target(TargetFilters.creature());

        // Sacrifice an untapped creature: Enchanted creature gets +2/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsTappedPredicate()))),
                                "an untapped creature",
                                false),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(0))),
                "Sacrifice an untapped creature: Enchanted creature gets +2/+0 until end of turn."
        ));

        // Sacrifice enchanted creature: Creatures you control get +2/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeEnchantedCreatureEffect(),
                        new BoostAllOwnCreaturesEffect(2, 0)),
                "Sacrifice enchanted creature: Creatures you control get +2/+0 until end of turn."
        ));
    }
}
