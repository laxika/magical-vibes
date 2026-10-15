package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "291")
public class HelmOfPossession extends Card {

    public HelmOfPossession() {
        // "You may choose not to untap this artifact during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // "{2}, {T}, Sacrifice a creature: Gain control of target creature for as long as you
        // control this artifact and this artifact remains tapped."
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice a creature"),
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_TAPPED)),
                "{2}, {T}, Sacrifice a creature: Gain control of target creature for as long as you control Helm of Possession and Helm of Possession remains tapped.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
