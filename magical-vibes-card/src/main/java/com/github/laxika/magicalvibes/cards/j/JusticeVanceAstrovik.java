package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "61")
public class JusticeVanceAstrovik extends Card {

    public JusticeVanceAstrovik() {
        target(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        "Target must be a nonland, nontoken permanent"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());
        addEffect(EffectSlot.ON_CONTROLLER_ANOTHER_NONLAND_PERMANENT_RETURNED_TO_HAND,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
