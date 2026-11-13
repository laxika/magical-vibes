package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "102")
@CardRegistration(set = "PIO", collectorNumber = "341")
public class GoblinHeelcutter extends Card {

    public GoblinHeelcutter() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target creature"
        )).addTrigger(Triggers.attacks(), new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
