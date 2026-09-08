package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "212")
public class WillowSatyr extends Card {

    public WillowSatyr() {
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_TAPPED)),
                "{T}: Gain control of target legendary creature for as long as you control Willow Satyr and Willow Satyr remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                        )),
                        "Target must be a legendary creature"
                )
        ));
    }
}
