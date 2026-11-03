package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayUntilAnotherEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "144")
@CardRegistration(set = "THB", collectorNumber = "136")
@CardRegistration(set = "PIO", collectorNumber = "136")
@CardRegistration(set = "CMM", collectorNumber = "225")
@CardRegistration(set = "FIC", collectorNumber = "294")
public class FuriousRise extends Card {

    public FuriousRise() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4))))),
                new ExileTopCardMayPlayUntilAnotherEffect());
    }
}
