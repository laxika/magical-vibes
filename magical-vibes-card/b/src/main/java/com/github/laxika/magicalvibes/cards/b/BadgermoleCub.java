package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "167")
public class BadgermoleCub extends Card {

    public BadgermoleCub() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EarthbendTargetLandEffect(1));
        addTrigger(Triggers.youTapCreatureForMana(),
                new AddManaWhenTappedForManaEffect(ManaColor.GREEN));
    }
}
