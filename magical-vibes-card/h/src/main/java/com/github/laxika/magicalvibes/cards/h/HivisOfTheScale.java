package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "182")
public class HivisOfTheScale extends Card {

    public HivisOfTheScale() {
        // "You may choose not to untap Hivis of the Scale during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // "{T}: Gain control of target Dragon for as long as you control Hivis of the Scale
        // and Hivis of the Scale remains tapped."
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_TAPPED)),
                "{T}: Gain control of target Dragon for as long as you control Hivis of the Scale and Hivis of the Scale remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        "Target must be a Dragon")));
    }
}
