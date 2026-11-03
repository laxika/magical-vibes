package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "155")
public class StampedeRider extends Card {

    public StampedeRider() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(), new PermanentPowerAtLeastPredicate(4))))),
                new BoostSelfEffect(1, 1));
    }
}
