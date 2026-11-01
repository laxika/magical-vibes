package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileThenGrantFreeCastPermissionEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "45")
@CardRegistration(set = "DSC", collectorNumber = "119")
@CardRegistration(set = "C19", collectorNumber = "89")
public class KheruSpellsnatcher extends Card {

    public KheruSpellsnatcher() {
        addMorph("{4}{U}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new CounterSpellAndExileThenGrantFreeCastPermissionEffect());
    }
}
