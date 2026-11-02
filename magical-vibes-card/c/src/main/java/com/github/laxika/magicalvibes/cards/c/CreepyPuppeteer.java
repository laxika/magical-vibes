package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfOtherAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "151")
@CardRegistration(set = "DBL", collectorNumber = "418")
public class CreepyPuppeteer extends Card {

    public CreepyPuppeteer() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new ExactlyAttackers(2),
                new MayEffect(
                        new SetBasePowerToughnessOfOtherAttackingCreatureEffect(4, 3),
                        "Have the other attacking creature become 4/3 until end of turn?")));
    }
}
