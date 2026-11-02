package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "99")
@CardRegistration(set = "SLD", collectorNumber = "1233")
@CardRegistration(set = "RVR", collectorNumber = "83")
@CardRegistration(set = "MAR", collectorNumber = "67")
@CardRegistration(set = "DSC", collectorNumber = "146")
@CardRegistration(set = "OTC", collectorNumber = "140")
public class MassacreGirl extends Card {

    public MassacreGirl() {
        CardEffect weakenOtherCreatures = new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), weakenOtherCreatures);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RegisterDelayedCreatureDeathTriggerEffect(weakenOtherCreatures));
    }
}
