package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "3")
@CardRegistration(set = "C20", collectorNumber = "76")
public class AstralDrift extends Card {

    public AstralDrift() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCycleCard(),
                new MayEffect(
                        FlickerEffect.exileTargetReturnAtEndStep(),
                        "Exile target creature?"))
                .addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                        FlickerEffect.exileTargetReturnAtEndStep(), "Exile target creature?"));
        addCycling("{2}{W}");
    }
}
