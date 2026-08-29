package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "13")
public class MiracleWorker extends Card {

    public MiracleWorker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect()),
                "{T}: Destroy target Aura attached to a creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAuraAttachedToCreaturePredicate(),
                        "Target must be an Aura attached to a creature"
                )
        ));
    }
}
