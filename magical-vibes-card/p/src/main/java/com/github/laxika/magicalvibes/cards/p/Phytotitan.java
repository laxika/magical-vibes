package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;

@CardRegistration(set = "M15", collectorNumber = "191")
public class Phytotitan extends Card {

    public Phytotitan() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), RegisterDelayedStepTriggerEffect.aboutCardAtNext(
                Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER), Subjects.sourceCard(),
                new PutCardsOntoBattlefieldEffect(Subjects.triggeringCards()).enteringTapped())
                .ofThatPlayer(Subjects.ownersOf(Subjects.sourceCard())));
    }
}
