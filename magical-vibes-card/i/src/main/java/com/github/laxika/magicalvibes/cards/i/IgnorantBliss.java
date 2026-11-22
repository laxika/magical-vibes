package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "64")
public class IgnorantBliss extends Card {

    public IgnorantBliss() {
        addSpellEffect(ExileTargetPlayerHandEffect.handsOf(Subjects.you()).exiledFaceDown());
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)),
                        new DrawCardEffect())
                .evenIfNothingHappened());
    }
}
