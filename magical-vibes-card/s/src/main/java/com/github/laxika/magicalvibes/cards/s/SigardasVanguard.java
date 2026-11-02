package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "8")
@CardRegistration(set = "MIC", collectorNumber = "46")
public class SigardasVanguard extends Card {

    public SigardasVanguard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect());
        addTrigger(Triggers.attacks(),
                new ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect());
    }
}
