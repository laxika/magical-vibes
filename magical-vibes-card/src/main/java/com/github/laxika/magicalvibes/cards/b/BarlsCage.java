package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "352")
public class BarlsCage extends Card {

    public BarlsCage() {
        // {3}: Target creature doesn't untap during its controller's next untap step.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "{3}: Target creature doesn't untap during its controller's next untap step.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )));
    }
}
