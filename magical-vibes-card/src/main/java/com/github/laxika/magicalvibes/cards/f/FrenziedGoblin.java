package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M15", collectorNumber = "142")
public class FrenziedGoblin extends Card {

    public FrenziedGoblin() {
        // Whenever this creature attacks, you may pay {R}. If you do, target creature can't block this turn.
        // The target is chosen as the trigger goes on the stack; the payment choice happens at resolution.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target creature"
        )).addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{R}",
                new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                "Pay {R} so target creature can't block this turn?"
        ));
    }
}
