package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "171")
public class MerrowGrimeblotter extends Card {

    public MerrowGrimeblotter() {
        // {1}{U/B}, {Q}: Target creature gets -2/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U/B}",
                List.of(new BoostTargetCreatureEffect(-2, 0)),
                "{1}{U/B}, {Q}: Target creature gets -2/-0 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ).withRequiresUntap());
    }
}
