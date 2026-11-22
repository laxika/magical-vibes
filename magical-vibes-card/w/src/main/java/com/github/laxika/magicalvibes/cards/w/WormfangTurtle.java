package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileChosenPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "60")
public class WormfangTurtle extends Card {

    public WormfangTurtle() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileChosenPermanentsEffect.youControl(new PermanentIsLandPredicate(), "land").withSource());
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceEffect());
    }
}
