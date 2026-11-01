package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "30")
public class WatcherOfTheRoost extends Card {

    public WatcherOfTheRoost() {
        addMorphWithRevealCost(new CardColorPredicate(CardColor.WHITE), "white");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
