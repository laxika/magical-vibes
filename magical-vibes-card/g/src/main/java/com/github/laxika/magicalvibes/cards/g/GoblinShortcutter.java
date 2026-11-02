package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "142")
@CardRegistration(set = "ZEN", collectorNumber = "128")
public class GoblinShortcutter extends Card {

    public GoblinShortcutter() {
        // When this creature enters, target creature can't block this turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
