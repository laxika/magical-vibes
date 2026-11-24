package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIO", collectorNumber = "1")
public class BaneOfBalaGed extends Card {

    public BaneOfBalaGed() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                ChoosePermanentsEffect.exactly(2, Subjects.controlledBy(Subjects.defendingPlayer()),
                                "two permanents you control to exile")
                        .chosenBy(Subjects.defendingPlayer()),
                new ExileTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE))));
    }
}
