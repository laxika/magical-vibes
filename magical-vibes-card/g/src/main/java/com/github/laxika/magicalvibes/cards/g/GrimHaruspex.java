package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "73")
@CardRegistration(set = "C19", collectorNumber = "118")
@CardRegistration(set = "SLD", collectorNumber = "969")
@CardRegistration(set = "SLD", collectorNumber = "2243")
@CardRegistration(set = "SPG", collectorNumber = "152")
public class GrimHaruspex extends Card {

    public GrimHaruspex() {
        addMorph("{B}");
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new DrawCardEffect(1));
    }
}
