package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "121")
public class WhiplashVengefulEngineer extends Card {

    public WhiplashVengefulEngineer() {
        addStaticEffect(new EntersTappedEffect());
        AttachmentsOnSource equipmentCount = new AttachmentsOnSource(false, true);
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new Equipped()),
                SequenceEffect.of(
                        new LoseLifeEffect(equipmentCount, Subjects.eachOpponent()),
                        new GainLifeEffect(equipmentCount)));
    }
}
