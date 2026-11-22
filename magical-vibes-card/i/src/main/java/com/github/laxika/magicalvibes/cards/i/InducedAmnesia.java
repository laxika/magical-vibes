package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "40")
public class InducedAmnesia extends Card {

    public InducedAmnesia() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ExileTargetPlayerHandEffect.handsOfWithSource(Subjects.targetPlayerDeclaredElsewhere())
                                .exiledFaceDown(),
                        new DrawCardEffect(new AmountThisWay(StepKind.EXILE), Subjects.targetPlayer())));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
