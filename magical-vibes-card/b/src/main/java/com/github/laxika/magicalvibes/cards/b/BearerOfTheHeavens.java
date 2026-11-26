package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

@CardRegistration(set = "JOU", collectorNumber = "89")
public class BearerOfTheHeavens extends Card {

    public BearerOfTheHeavens() {
        // When this creature dies, destroy all permanents at the beginning of the next end step.
        addTrigger(Triggers.dies(ObjectRelation.THIS), RegisterDelayedStepTriggerEffect.atNext(
                Triggers.endStep(ActivePlayer.ANY), DestroyTargetPermanentEffect.all(new PermanentTruePredicate())));
    }
}
