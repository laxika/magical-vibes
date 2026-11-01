package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "40")
public class InducedAmnesia extends Card {

    public InducedAmnesia() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPlayerHandFaceDownWithSourceThenDrawEffect());
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
