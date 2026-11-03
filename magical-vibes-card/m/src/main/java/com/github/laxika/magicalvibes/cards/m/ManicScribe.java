package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "73")
@CardRegistration(set = "SIR", collectorNumber = "80")
public class ManicScribe extends Card {

    public ManicScribe() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, MillRecipient.EACH_OPPONENT));
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
