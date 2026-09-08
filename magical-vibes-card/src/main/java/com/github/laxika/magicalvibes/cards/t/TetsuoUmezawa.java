package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "262")
public class TetsuoUmezawa extends Card {

    public TetsuoUmezawa() {
        addEffect(EffectSlot.STATIC, new CantBeEnchantedByOtherAurasEffect());

        var targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsTappedPredicate(),
                        new PermanentIsBlockingPredicate()
                ))
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{B}{B}{R}",
                List.of(new DestroyTargetPermanentEffect(targetPredicate)),
                "{U}{B}{B}{R}, {T}: Destroy target tapped or blocking creature.",
                new PermanentPredicateTargetFilter(
                        targetPredicate,
                        "Target must be a tapped or blocking creature"
                )
        ));
    }
}
