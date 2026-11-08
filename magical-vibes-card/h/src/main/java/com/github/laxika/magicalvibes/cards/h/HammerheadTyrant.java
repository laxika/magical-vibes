package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "21")
@CardRegistration(set = "TDC", collectorNumber = "61")
public class HammerheadTyrant extends Card {

    public HammerheadTyrant() {
        // "With mana value X or less, where X is that spell's mana value": the target is chosen as the ability goes
        // on the stack, against the spell's mana value as the ability's X.
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentMaxManaValueXPredicate())),
                        "Target must be a nonland permanent an opponent controls with mana value X or less"), 0, 1)
                .addTrigger(Triggers.youCastSpell().anySpell(), ReturnToHandEffect.target());
    }
}
