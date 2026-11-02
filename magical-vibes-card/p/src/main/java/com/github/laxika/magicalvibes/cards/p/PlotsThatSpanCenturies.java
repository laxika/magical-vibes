package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextSchemeSetInMotionReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SchemeSetInMotionTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "351")
public class PlotsThatSpanCenturies extends Card {

    public PlotsThatSpanCenturies() {
        addTrigger(Triggers.youSetSchemeInMotion(),
                new SchemeSetInMotionTriggerEffect(List.of(
                        new RegisterNextSchemeSetInMotionReplacementEffect())));
    }
}
