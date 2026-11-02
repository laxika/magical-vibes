package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "107")
@CardRegistration(set = "TPR", collectorNumber = "171")
public class EndangeredArmodon extends Card {

    public EndangeredArmodon() {
        addTrigger(Triggers.state(), new StateTriggerEffect(
                new PermanentControllerControlsPermanentPredicate(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentToughnessAtMostPredicate(2)
                        ))),
                List.of(new SacrificeSelfEffect()),
                "Endangered Armodon's state-triggered ability"
        ));
    }
}
