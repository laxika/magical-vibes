package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HML", collectorNumber = "13a")
@CardRegistration(set = "HML", collectorNumber = "13b")
public class SamiteAlchemist extends Card {

    public SamiteAlchemist() {
        // {W}{W}, {T}: Prevent the next 4 damage that would be dealt this turn to target creature
        // you control. Tap that creature. It doesn't untap during your next untap step.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(
                        PreventDamageEffect.nextToTarget(4),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)
                ),
                "{W}{W}, {T}: Prevent the next 4 damage that would be dealt this turn to target creature you control. "
                        + "Tap that creature. It doesn't untap during your next untap step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
