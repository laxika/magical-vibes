package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "194")
public class OrneryDilophosaur extends Card {

    public OrneryDilophosaur() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4))))),
                BoostEffect.self(2, 2));
    }
}
