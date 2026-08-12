package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "83")
public class RubiniaSoulsinger extends Card {

    public RubiniaSoulsinger() {
        // "You may choose not to untap Rubinia Soulsinger during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // "{T}: Gain control of target creature for as long as you control Rubinia Soulsinger
        // and Rubinia Soulsinger remains tapped."
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_TAPPED)),
                "{T}: Gain control of target creature for as long as you control Rubinia Soulsinger and Rubinia Soulsinger remains tapped.",
                TargetFilters.creature()));
    }
}
