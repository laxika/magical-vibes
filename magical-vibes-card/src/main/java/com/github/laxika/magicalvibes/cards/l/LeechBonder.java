package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "43")
public class LeechBonder extends Card {

    public LeechBonder() {
        // "This creature enters with two -1/-1 counters on it."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSourceEffect(-1, -1, 2));

        // "{U}, {Q}: Move a counter from target creature onto a second target creature."
        addActivatedAbility(new ActivatedAbility(
                false,  // {Q} untaps rather than taps
                "{U}",
                List.of(new MoveCounterFromTargetCreatureToTargetCreatureEffect()),
                "{U}, {Q}: Move a counter from target creature onto a second target creature.",
                List.of(
                        new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "First target must be a creature"),
                        new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Second target must be a creature")
                ),
                2,  // minTargets
                2   // maxTargets
        ).withRequiresUntap());
    }
}
