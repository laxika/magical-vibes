package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCreatureCardsToBattlefieldAndShuffleRestEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "15")
@CardRegistration(set = "KHC", collectorNumber = "44")
public class SyntheticDestiny extends Card {

    public SyntheticDestiny() {
        addSpellEffect(new ExileTargetPermanentEffect(Subjects.creaturesYouControl()));
        addSpellEffect(RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new RevealCreatureCardsToBattlefieldAndShuffleRestEffect(new AmountThisWay(StepKind.EXILE)))
                .evenIfNothingHappened());
    }
}
