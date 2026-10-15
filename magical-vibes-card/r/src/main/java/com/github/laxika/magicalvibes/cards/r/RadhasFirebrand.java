package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "141")
public class RadhasFirebrand extends Card {

    public RadhasFirebrand() {
        PermanentPredicate lowerPowerDefendingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate(),
                new PermanentPowerLessThanSourcePowerPredicate()));

        target(new PermanentPredicateTargetFilter(
                lowerPowerDefendingCreature,
                "Target must be a creature defending player controls with power less than Radha's Firebrand's power"))
                .addEffect(EffectSlot.ON_ATTACK, new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        addEffect(EffectSlot.STATIC, new ReduceActivatedAbilityCostEffect(
                new PermanentIsSourcePermanentPredicate(),
                new BasicLandTypesAmongControlledLands(),
                false));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(new BoostSelfEffect(2, 2)),
                "{5}{R}: This creature gets +2/+2 until end of turn.",
                1));
    }
}
