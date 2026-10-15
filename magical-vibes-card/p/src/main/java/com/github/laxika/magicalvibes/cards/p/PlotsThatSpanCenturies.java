package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterNextSchemeSetInMotionReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SchemeSetInMotionTriggerEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "351")
public class PlotsThatSpanCenturies extends Card {

    public PlotsThatSpanCenturies() {
        addEffect(EffectSlot.ON_CONTROLLER_SETS_SCHEME_IN_MOTION,
                new SchemeSetInMotionTriggerEffect(List.of(
                        new RegisterNextSchemeSetInMotionReplacementEffect())));
    }
}
