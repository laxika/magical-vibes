package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "138")
public class BurningSunCavalry extends Card {

    public BurningSunCavalry() {
        ControlsPermanent controlsDinosaur = new ControlsPermanent(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR));
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()),
                TriggerOptions.ONE_OR_MORE.withEventCondition(controlsDinosaur), new BoostSelfEffect(1, 1));
    }
}
