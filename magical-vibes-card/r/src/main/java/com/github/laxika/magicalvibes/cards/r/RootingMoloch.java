package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasCyclingPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "133")
public class RootingMoloch extends Card {

    public RootingMoloch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(new CardHasCyclingPredicate()),
                AllowPlayCardsFromExileEffect.untilEndOfYourNextTurn(Subjects.cardsThisWay(StepKind.EXILE)));
        addCycling("{2}");
    }
}
