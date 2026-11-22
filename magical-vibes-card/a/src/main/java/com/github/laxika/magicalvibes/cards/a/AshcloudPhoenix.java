package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "99")
@CardRegistration(set = "MKC", collectorNumber = "147")
public class AshcloudPhoenix extends Card {

    public AshcloudPhoenix() {
        addMorph("{4}{R}{R}");
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards())
                        .underControlOf(Subjects.you())
                        .faceDown());
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new DealDamageToPlayersEffect(2, Subjects.eachPlayer()));
    }
}
