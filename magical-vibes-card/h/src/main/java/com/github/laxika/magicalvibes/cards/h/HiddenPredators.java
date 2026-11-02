package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "263")
public class HiddenPredators extends Card {

    public HiddenPredators() {
        addTrigger(Triggers.state(), new StateTriggerEffect(
                null, new PermanentIsEnchantmentPredicate(), null, 0, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                List.of(new BecomeCreatureEffect(4, 4, CardSubtype.BEAST)),
                "Hidden Predators's state-triggered ability"
        ));
    }
}
