package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

@CardRegistration(set = "TMT", collectorNumber = "88")
@CardRegistration(set = "TMT", collectorNumber = "235")
@CardRegistration(set = "TMT", collectorNumber = "286")
@CardRegistration(set = "TMT", collectorNumber = "296")
public class CaseyJonesVigilante extends Card {

    public CaseyJonesVigilante() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(3),
                RegisterDelayedStepTriggerEffect.atNext(
                        Triggers.upkeep(ActivePlayer.YOU), new DiscardEffect(3, Subjects.you(), true)));
    }
}
