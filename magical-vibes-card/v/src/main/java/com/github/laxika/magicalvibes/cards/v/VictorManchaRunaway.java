package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "574")
public class VictorManchaRunaway extends Card {

    public VictorManchaRunaway() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.oneTargetFromControllerGraveyardWithSource(null),
                AllowPlayCardsFromExileEffect.whileYouControlSource(Subjects.cardsThisWay(StepKind.EXILE)));
    }
}
