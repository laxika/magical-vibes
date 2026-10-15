package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SuspectEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSuspectedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "229")
public class RuneBrandJuggler extends Card {

    public RuneBrandJuggler() {
        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SuspectEffect(GrantScope.TARGET));

        var suspectedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsSuspectedPredicate()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{R}",
                List.of(
                        new SacrificePermanentCost(suspectedCreature, "a suspected creature", false),
                        new BoostTargetCreatureEffect(-5, -5)
                ),
                "{3}{B}{R}, Sacrifice a suspected creature: Target creature gets -5/-5 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
