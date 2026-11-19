package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SCG", collectorNumber = "48")
@CardRegistration(set = "C14", collectorNumber = "121")
public class RiptideSurvivor extends Card {

    public RiptideSurvivor() {
        addMorph("{1}{U}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new DiscardEffect(2, Subjects.you()),
                new DrawCardEffect(3));
    }
}
