package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AnotherCreatureOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEventTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "139")
public class Portcullis extends Card {

    public Portcullis() {
        // "If there are two or more other creatures on the battlefield": checked as the creature enters and again
        // as the ability resolves (CR 603.4).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield(2)),
                SequenceEffect.of(
                        ExileTargetPermanentEffect.ofTriggeringPermanent(),
                        RegisterDelayedEventTriggerEffect.whenSourceLeaves(
                                ReturnAllCardsExiledWithSourceEffect.returnToBattlefield(
                                        Subjects.cardsThisWay(StepKind.EXILE)))));
    }
}
