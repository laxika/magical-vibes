package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "148")
public class SilkbindFaerie extends Card {

    public SilkbindFaerie() {
        // {1}{W/U}, {Q}: Tap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W/U}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}{W/U}, {Q}: Tap target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ).withRequiresUntap());
    }
}
