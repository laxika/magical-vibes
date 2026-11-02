package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPermanentsThenPhaseOutRestEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsCantPhaseInEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRC", collectorNumber = "21")
@CardRegistration(set = "BRC", collectorNumber = "41")
public class DiscipleOfCaelusNin extends Card {

    public DiscipleOfCaelusNin() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerChoosesPermanentsThenPhaseOutRestEffect(5));
        addStaticEffect(new PermanentsCantPhaseInEffect());
    }
}
