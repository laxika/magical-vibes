package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "36")
public class Fatespinner extends Card {

    public Fatespinner() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new TargetPlayerChoosesOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw step",
                        new SkipChosenStepOrPhaseEffect(SkipStepOrPhaseKind.DRAW_STEP)),
                new ChooseOneEffect.ChooseOneOption("Main phase",
                        new SkipChosenStepOrPhaseEffect(SkipStepOrPhaseKind.MAIN_PHASE)),
                new ChooseOneEffect.ChooseOneOption("Combat phase",
                        new SkipChosenStepOrPhaseEffect(SkipStepOrPhaseKind.COMBAT_PHASE))
        )));
    }
}
