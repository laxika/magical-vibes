package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "318")
public class SandGolem extends Card {

    public SandGolem() {
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                RegisterDelayedStepTriggerEffect.aboutCardAtNext(
                        Triggers.endStep(ActivePlayer.ANY), Subjects.sourceCard(),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, false,
                                CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
