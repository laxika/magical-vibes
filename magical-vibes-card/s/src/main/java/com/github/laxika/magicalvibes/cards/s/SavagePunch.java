package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "147")
@CardRegistration(set = "PIO", collectorNumber = "192")
public class SavagePunch extends Card {

    public SavagePunch() {
        // Ferocious — the creature you control gets +2/+2 until end of turn before it fights.
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new ConditionalEffect(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4)))),
                        new BoostTargetCreatureEffect(2, 2)));

        // Target creature you control fights target creature you don't control.
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new FightTargetsEffect());
    }
}
