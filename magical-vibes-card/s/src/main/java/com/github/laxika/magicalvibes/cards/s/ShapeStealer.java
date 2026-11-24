package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "55")
public class ShapeStealer extends Card {

    public ShapeStealer() {
        SetBasePowerToughnessToAmountEffect becomeThatCreature = new SetBasePowerToughnessToAmountEffect(
                new TriggeringPermanentPower(true), new TriggeringPermanentToughness(true), Subjects.source(),
                Duration.UNTIL_END_OF_TURN);
        addTrigger(Triggers.blocks(), becomeThatCreature);
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                becomeThatCreature);
    }
}
