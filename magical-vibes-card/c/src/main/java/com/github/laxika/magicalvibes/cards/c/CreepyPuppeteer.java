package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "151")
@CardRegistration(set = "DBL", collectorNumber = "418")
public class CreepyPuppeteer extends Card {

    public CreepyPuppeteer() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new ExactlyAttackers(2)),
                new MayEffect(
                        new SetBasePowerToughnessEffect(4, 3,
                                Subjects.except(Subjects.triggeringAttackers(), Subjects.source()),
                                Duration.UNTIL_END_OF_TURN),
                        "Have the other attacking creature become 4/3 until end of turn?"));
    }
}
